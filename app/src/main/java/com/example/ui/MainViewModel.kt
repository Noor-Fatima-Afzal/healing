package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.sync.FirebaseSyncManager
import com.example.utils.DailyContentProvider
import com.example.utils.DynamicIslamicContent
import com.example.utils.ContentType
import com.example.utils.DuaContent
import com.example.utils.HadithContent
import com.example.utils.QuranVerse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val sharedPrefs = application.getSharedPreferences("healing_with_quran_prefs", Context.MODE_PRIVATE)

    // Sync status flows from FirebaseSyncManager
    val isOnline: StateFlow<Boolean> = FirebaseSyncManager.getInstance(application).isOnline
    val syncStatus: StateFlow<String> = FirebaseSyncManager.getInstance(application).syncStatus

    // UI States
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _studentStatusFilter = MutableStateFlow("All") // "All", "Active", "Inactive"
    val studentStatusFilter: StateFlow<String> = _studentStatusFilter.asStateFlow()

    // Favorite Duas (stored as string of IDs locally)
    private val _favoriteDuas = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteDuas: StateFlow<Set<Int>> = _favoriteDuas.asStateFlow()

    private val _randomIslamicContent = MutableStateFlow<DynamicIslamicContent?>(null)
    val randomIslamicContent: StateFlow<DynamicIslamicContent?> = _randomIslamicContent.asStateFlow()

    fun loadRandomIslamicContent() {
        viewModelScope.launch {
            try {
                // Ensure the database is prepopulated first
                repository.prepopulateIfEmpty()
                
                // Fetch least recently displayed items (ordered by lastDisplayedAt ASC)
                val allContent = repository.getAllIslamicContentSortedByLastDisplayed()
                if (allContent.isNotEmpty()) {
                    // Choose randomly from the 5 least recently displayed items (or list size if smaller)
                    val poolSize = if (allContent.size < 5) allContent.size else 5
                    val pool = allContent.subList(0, poolSize)
                    val selected = pool.random()
                    
                    val dynamicContent = DynamicIslamicContent(
                        type = try { ContentType.valueOf(selected.type) } catch (e: Exception) { ContentType.AYAH },
                        arabicText = selected.arabicText,
                        urduTranslation = selected.urduTranslation,
                        englishTranslation = selected.englishTranslation,
                        surahName = selected.surahName,
                        ayahNumber = selected.ayahNumber,
                        reference = selected.reference,
                        bookName = selected.bookName,
                        source = selected.source
                    )
                    
                    _randomIslamicContent.value = dynamicContent
                    
                    // Update lastDisplayedAt to current time so it won't be shown again immediately
                    repository.updateIslamicContent(selected.copy(lastDisplayedAt = System.currentTimeMillis()))
                } else {
                    _randomIslamicContent.value = DailyContentProvider.getRandomContent(getApplication())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _randomIslamicContent.value = DailyContentProvider.getRandomContent(getApplication())
            }
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(
            userDao = database.userDao(),
            studentDao = database.studentDao(),
            attendanceDao = database.attendanceDao(),
            lessonDao = database.lessonDao(),
            homeworkDao = database.homeworkDao(),
            testDao = database.testDao(),
            importantNoteDao = database.importantNoteDao(),
            islamicContentDao = database.islamicContentDao()
        )
        repository.initSync(application)
        FirebaseSyncManager.getInstance(application).startSyncing()

        // Load SharedPreferences states
        _isOnboardingCompleted.value = sharedPrefs.getBoolean("onboarding_completed", false)
        _isDarkMode.value = sharedPrefs.getBoolean("dark_mode", false)
        
        // Load dynamic Islamic content from local DB
        loadRandomIslamicContent()
        
        // Auto sign-in if remembered
        val savedUid = sharedPrefs.getString("saved_uid", null)
        val savedEmail = sharedPrefs.getString("saved_email", null)
        if (savedUid != null && savedEmail != null) {
            viewModelScope.launch {
                var user = repository.getUserByUid(savedUid)
                if (user == null) {
                    user = repository.getUserByEmail(savedEmail)
                }
                if (user != null) {
                    _currentUser.value = user
                }
            }
        }

        // Initialize/prepopulate database if empty
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // Load favorited Duas
        val favs = sharedPrefs.getStringSet("favorite_duas", emptySet()) ?: emptySet()
        _favoriteDuas.value = favs.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // Exposed Flows from Room
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLessons: StateFlow<List<Lesson>> = repository.allLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLessons: StateFlow<List<Lesson>> = repository.recentLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHomework: StateFlow<List<Homework>> = repository.allHomework
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTests: StateFlow<List<Test>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<ImportantNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Students Flow (Search + Status)
    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        _studentSearchQuery,
        _studentStatusFilter
    ) { students, query, statusFilter ->
        students.filter { student ->
            val matchesQuery = student.fullName.contains(query, ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                "Active" -> student.status == "Active"
                "Inactive" -> student.status == "Inactive"
                else -> true
            }
            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily rotating contents based on current day of month
    val todayDayOfMonth: Int
        get() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val todayQuranVerse: QuranVerse
        get() = DailyContentProvider.getQuranVerseForDay(todayDayOfMonth)

    val todayHadith: HadithContent
        get() = DailyContentProvider.getHadithForDay(todayDayOfMonth)

    val todayDua: DuaContent
        get() = DailyContentProvider.getDuaForDay(todayDayOfMonth)

    val todayReminder: String
        get() = DailyContentProvider.getReminderForDay(todayDayOfMonth)

    // Gregorian & Hijri Dates
    val gregorianDate: String
        get() {
            val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            return sdf.format(Date())
        }

    val hijriDate: String
        get() {
            return try {
                val uLocale = android.icu.util.ULocale("en_US@calendar=islamic-umalqura")
                val sdf = android.icu.text.SimpleDateFormat("d MMMM yyyy", uLocale)
                val formatted = sdf.format(java.util.Date())
                "$formatted AH"
            } catch (e: Exception) {
                // Safe dynamic fallback
                val calendar = Calendar.getInstance()
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                val baseYear = 1448
                val hMonth = when (month) {
                    5 -> "Muharram" // June
                    6 -> "Safar"    // July
                    7 -> "Rabi' al-Awwal"
                    8 -> "Rabi' al-Thani"
                    9 -> "Jumada al-Awwal"
                    10 -> "Jumada al-Thani"
                    11 -> "Rajab"
                    0 -> "Sha'ban"
                    1 -> "Ramadan"
                    2 -> "Shawwal"
                    3 -> "Dhu al-Qi'dah"
                    4 -> "Dhu al-Hijjah"
                    else -> "Safar"
                }
                val hDay = (day + 15) % 30 + 1
                val hYear = baseYear + (year - 2026)
                "$hDay $hMonth $hYear AH"
            }
        }

    // Actions
    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun toggleDarkMode() {
        val newVal = !_isDarkMode.value
        _isDarkMode.value = newVal
        sharedPrefs.edit().putBoolean("dark_mode", newVal).apply()
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setStudentStatusFilter(status: String) {
        _studentStatusFilter.value = status
    }

    private fun findStudentForEmail(email: String, students: List<Student>): Student? {
        val prefix = email.substringBefore("@").lowercase().replace("_", "").replace(".", "").replace(" ", "")
        return students.find { student ->
            val cleanStudentName = student.fullName.lowercase().replace(" ", "")
            cleanStudentName == prefix || cleanStudentName.contains(prefix) || prefix.contains(cleanStudentName)
        }
    }

    /**
     * Checks if Firebase is properly configured with a valid API Key rather than dummy placeholders.
     */
    fun isFirebaseConfigured(): Boolean {
        return try {
            val app = com.google.firebase.FirebaseApp.getInstance()
            val apiKey = app.options.apiKey
            apiKey.isNotEmpty() && apiKey != "dummy-api-key" && !apiKey.contains("dummy")
        } catch (e: Exception) {
            false
        }
    }

    // SUPPORTS USER SIGN IN
    fun login(email: String, password: String, rememberMe: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            onError("Please fill in all fields.")
            return
        }

        val auth = try {
            com.google.firebase.auth.FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

        if (auth != null && isOnline.value && isFirebaseConfigured()) {
            auth.signInWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user ?: auth.currentUser
                        if (firebaseUser != null) {
                            val uid = firebaseUser.uid
                            viewModelScope.launch {
                                var user = repository.getUserByUid(uid)
                                if (user == null) {
                                    user = repository.getUserByEmail(cleanEmail)
                                    if (user != null) {
                                        val updatedUser = user.copy(uid = uid)
                                        repository.insertUser(updatedUser)
                                        user = updatedUser
                                    } else {
                                        val studentsList = repository.getAllStudents()
                                        val matchedStudent = findStudentForEmail(cleanEmail, studentsList)
                                        val (name, role) = if (matchedStudent != null) {
                                            Pair(matchedStudent.fullName, "Student")
                                        } else {
                                            val expectedRole = when {
                                                cleanEmail.contains("teacher") -> "Teacher"
                                                cleanEmail.contains("helper") -> "Helper"
                                                else -> "Student"
                                            }
                                            val namePart = cleanEmail.substringBefore("@")
                                                .replace(".", " ")
                                                .replace("_", " ")
                                                .split(" ")
                                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                                            Pair(namePart, expectedRole)
                                        }
                                        user = User(uid = uid, name = name, email = cleanEmail, role = role)
                                        repository.insertUser(user)
                                    }
                                }
                                _currentUser.value = user
                                if (rememberMe) {
                                    sharedPrefs.edit()
                                        .putString("saved_uid", uid)
                                        .putString("saved_email", cleanEmail)
                                        .apply()
                                }
                                onSuccess()
                            }
                        } else {
                            onError("Failed to retrieve authenticated user details.")
                        }
                    } else {
                        // Attempt fallback account checking and automatic Firebase Auth user creation!
                        viewModelScope.launch {
                            val localUser = repository.getUserByEmail(cleanEmail)
                            val expectedPassword = when {
                                cleanEmail.contains("teacher") || (localUser?.role == "Teacher") -> "teacher123"
                                cleanEmail.contains("helper") || (localUser?.role == "Helper") -> "helper123"
                                else -> "student123"
                            }
                            if (password == expectedPassword) {
                                auth.createUserWithEmailAndPassword(cleanEmail, password)
                                    .addOnCompleteListener { createCtx ->
                                        if (createCtx.isSuccessful) {
                                            val newFbUser = createCtx.result?.user ?: auth.currentUser
                                            if (newFbUser != null) {
                                                val uid = newFbUser.uid
                                                viewModelScope.launch {
                                                    val finalUser = localUser?.copy(uid = uid) ?: run {
                                                        val studentsList = repository.getAllStudents()
                                                        val matchedStudent = findStudentForEmail(cleanEmail, studentsList)
                                                        val (name, role) = if (matchedStudent != null) {
                                                            Pair(matchedStudent.fullName, "Student")
                                                        } else {
                                                            val namePart = cleanEmail.substringBefore("@")
                                                                .replace(".", " ")
                                                                .replace("_", " ")
                                                                .split(" ")
                                                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                                                            Pair(namePart, "Student")
                                                        }
                                                        User(uid = uid, name = name, email = cleanEmail, role = role)
                                                    }
                                                    repository.insertUser(finalUser)
                                                    _currentUser.value = finalUser
                                                    if (rememberMe) {
                                                        sharedPrefs.edit()
                                                            .putString("saved_uid", uid)
                                                            .putString("saved_email", cleanEmail)
                                                            .apply()
                                                    }
                                                    onSuccess()
                                                }
                                            } else {
                                                onError("Created user is null.")
                                            }
                                        } else {
                                            val exception = createCtx.exception
                                            val localizedMessage = exception?.localizedMessage ?: "Unknown Firebase User creation error."
                                            var customMessage = "Firebase Authentication error: $localizedMessage"
                                            
                                            if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                                                customMessage = "No user account exists with this email address ($cleanEmail)."
                                            } else if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                                customMessage = "Incorrect password or invalid email format."
                                            } else if (exception is com.google.firebase.FirebaseNetworkException) {
                                                customMessage = "Network error occurred. Please check your internet connection and try again."
                                            } else if (localizedMessage.contains("API key not valid", ignoreCase = true)) {
                                                customMessage = "The Firebase API Key is invalid. Please verify google-services.json and build configurations."
                                            }
                                            onError(customMessage)
                                        }
                                    }
                            } else {
                                val exception = task.exception
                                val localizedMessage = exception?.localizedMessage ?: "Invalid credentials."
                                var customMessage = "Authentication Failed: $localizedMessage"
                                
                                if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                                    customMessage = "No user account exists with this email address ($cleanEmail)."
                                } else if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                    customMessage = "Incorrect password or invalid email format."
                                } else if (exception is com.google.firebase.FirebaseNetworkException) {
                                    customMessage = "Network error occurred. Please check your internet connection and try again."
                                } else if (localizedMessage.contains("API key not valid", ignoreCase = true)) {
                                    customMessage = "The Firebase API Key is invalid. Please verify google-services.json and build configurations."
                                }
                                onError(customMessage)
                            }
                        }
                    }
                }
        } else {
            // Local fallback (offline, Firebase not configured, or using dummy API key)
            viewModelScope.launch {
                val user = repository.getUserByEmail(cleanEmail)
                if (user != null) {
                    val expectedPassword = when (user.role) {
                        "Teacher" -> "teacher123"
                        "Helper" -> "helper123"
                        "Student" -> "student123"
                        else -> "password123"
                    }

                    if (password == expectedPassword) {
                        _currentUser.value = user
                        if (rememberMe) {
                            sharedPrefs.edit()
                                .putString("saved_uid", user.uid)
                                .putString("saved_email", user.email)
                                .apply()
                        }
                        onSuccess()
                    } else {
                        onError("Invalid password.")
                    }
                } else {
                    val studentsList = repository.getAllStudents()
                    val matchedStudent = findStudentForEmail(cleanEmail, studentsList)
                    if (matchedStudent != null && password == "student123") {
                        val localUid = "uid_" + matchedStudent.fullName.lowercase().replace(" ", "_")
                        val newUser = User(uid = localUid, name = matchedStudent.fullName, email = cleanEmail, role = "Student")
                        repository.insertUser(newUser)
                        _currentUser.value = newUser
                        if (rememberMe) {
                            sharedPrefs.edit()
                                .putString("saved_uid", localUid)
                                .putString("saved_email", cleanEmail)
                                .apply()
                        }
                        onSuccess()
                    } else {
                        onError("Account not found. Use teacher@healing.com, helper@healing.com, or any student's email with password student123.")
                    }
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        sharedPrefs.edit()
            .remove("saved_uid")
            .remove("saved_email")
            .apply()
    }

    fun enrollUser(
        name: String,
        email: String,
        role: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        if (name.isBlank() || email.isBlank() || role.isBlank()) {
            onError("All fields are required.")
            return
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            onError("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            try {
                val existing = repository.getUserByEmail(cleanEmail)
                if (existing != null) {
                    onError("An account with this email already exists.")
                    return@launch
                }

                val defaultPassword = if (role == "Teacher") "teacher123" else "helper123"
                val auth = try {
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                } catch (e: Exception) {
                    null
                }
                val hasFirebase = isFirebaseConfigured() && auth != null

                if (hasFirebase && auth != null) {
                    auth.createUserWithEmailAndPassword(cleanEmail, defaultPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val fbUser = task.result?.user ?: auth.currentUser
                                val uid = fbUser?.uid ?: ("uid_" + System.currentTimeMillis())
                                viewModelScope.launch {
                                    val newUser = User(uid = uid, name = name, email = cleanEmail, role = role)
                                    repository.insertUser(newUser)
                                    _currentUser.value = newUser
                                    sharedPrefs.edit()
                                        .putString("saved_uid", uid)
                                        .putString("saved_email", cleanEmail)
                                        .apply()
                                    onSuccess()
                                }
                            } else {
                                val localizedMessage = task.exception?.localizedMessage ?: "User registration failed."
                                onError(localizedMessage)
                            }
                        }
                } else {
                    val localUid = "uid_" + System.currentTimeMillis()
                    val newUser = User(uid = localUid, name = name, email = cleanEmail, role = role)
                    repository.insertUser(newUser)
                    _currentUser.value = newUser
                    sharedPrefs.edit()
                        .putString("saved_uid", localUid)
                        .putString("saved_email", cleanEmail)
                        .apply()
                    onSuccess()
                }
            } catch (e: Exception) {
                onError("Enrollment failed: ${e.localizedMessage}")
            }
        }
    }

    fun changePassword(old: String, new: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (old.isEmpty() || new.isEmpty()) {
            onError("Passwords cannot be empty.")
            return
        }
        if (new.length < 6) {
            onError("New password must be at least 6 characters.")
            return
        }
        onSuccess()
    }

    fun toggleDuaFavorite(duaId: Int) {
        val current = _favoriteDuas.value.toMutableSet()
        if (current.contains(duaId)) {
            current.remove(duaId)
        } else {
            current.add(duaId)
        }
        _favoriteDuas.value = current
        sharedPrefs.edit().putStringSet("favorite_duas", current.map { it.toString() }.toSet()).apply()
    }

    // DATABASE WRITE METHODS
    fun addStudent(fullName: String, status: String, generalNotes: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (fullName.trim().isEmpty()) {
            onError("Student Name cannot be empty.")
            return
        }
        viewModelScope.launch {
            val student = Student(
                fullName = fullName.trim(),
                status = status,
                generalNotes = generalNotes.trim()
            )
            repository.insertStudent(student)
            onComplete()
        }
    }

    fun updateStudent(studentId: Int, fullName: String, status: String, generalNotes: String, currentSurah: String, currentAyah: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (fullName.trim().isEmpty()) {
            onError("Student Name cannot be empty.")
            return
        }
        viewModelScope.launch {
            val existing = repository.getStudentById(studentId)
            if (existing != null) {
                val updated = existing.copy(
                    fullName = fullName.trim(),
                    status = status,
                    generalNotes = generalNotes.trim(),
                    currentSurah = currentSurah.trim(),
                    currentAyah = currentAyah.trim(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateStudent(updated)
                onComplete()
            } else {
                onError("Student not found.")
            }
        }
    }

    fun deleteStudent(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteStudentById(id)
            onComplete()
        }
    }

    // Attendance records on a specific date
    fun recordAttendance(date: String, attendanceMap: Map<Int, Pair<String, String>>, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (date.isEmpty()) {
            onError("Date cannot be empty.")
            return
        }
        viewModelScope.launch {
            val listToSave = mutableListOf<Attendance>()
            val dayName = try {
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val parsedDate = sdfInput.parse(date)
                if (parsedDate != null) {
                    java.text.SimpleDateFormat("EEEE", java.util.Locale.US).format(parsedDate)
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
            val timeString = try {
                java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
            } catch (e: Exception) {
                "17:30"
            }
            val recorderName = _currentUser.value?.name ?: "Bint e Khalid"

            for ((studentId, pair) in attendanceMap) {
                val (status, notes) = pair
                val student = repository.getStudentById(studentId)
                if (student != null) {
                    // Delete previous entry for this student on this date to prevent duplicate records
                    repository.deleteAttendanceForStudentOnDate(studentId, date)
                    
                    listToSave.add(
                        Attendance(
                            studentId = studentId,
                            studentName = student.fullName,
                            date = date,
                            day = dayName,
                            time = timeString,
                            status = status,
                            recordedBy = recorderName,
                            teacherNotes = notes
                        )
                    )

                    // Also recalculate student metrics (classes attended/missed/percentage)
                    updateStudentAttendanceMetrics(studentId, date, status)
                }
            }
            if (listToSave.isNotEmpty()) {
                repository.insertAllAttendance(listToSave)
            }
            onComplete()
        }
    }

    fun deleteAttendanceRecord(record: Attendance, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAttendanceById(record.id)
            updateStudentAttendanceMetrics(record.studentId, record.date, "")
            onComplete()
        }
    }

    private suspend fun updateStudentAttendanceMetrics(studentId: Int, newDate: String, newStatus: String) {
        val student = repository.getStudentById(studentId)
        if (student != null) {
            // Get all historical attendance for this student (we collect first values of the flow)
            val history = repository.getAttendanceForStudentFlow(studentId).firstOrNull() ?: emptyList()
            
            // Build the virtual history including this new or updated entry
            val filteredHistory = history.filter { it.date != newDate }.toMutableList()
            if (newStatus.isNotEmpty()) {
                filteredHistory.add(
                    Attendance(
                        studentId = studentId,
                        studentName = student.fullName,
                        date = newDate,
                        status = newStatus
                    )
                )
            }

            val attended = filteredHistory.count { it.status == "Present" }
            val missed = filteredHistory.count { it.status == "Absent" }
            val total = attended + missed
            val percentage = if (total > 0) (attended * 100) / total else 0

            val updatedStudent = student.copy(
                classesAttended = attended,
                classesMissed = missed,
                attendancePercentage = percentage,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateStudent(updatedStudent)
        }
    }

    // Save Daily Tafseer Lesson
    fun addLesson(lessonDate: String, lessonTitle: String, surahName: String, ayahNumber: String, homeworkDesc: String, teacherNotes: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (lessonTitle.trim().isEmpty()) {
            onError("Lesson title cannot be empty.")
            return
        }
        if (surahName.trim().isEmpty()) {
            onError("Surah Name cannot be empty.")
            return
        }
        if (ayahNumber.trim().isEmpty()) {
            onError("Ayah Number cannot be empty.")
            return
        }

        viewModelScope.launch {
            val creator = _currentUser.value?.name ?: "Bint e Khalid"
            val lesson = Lesson(
                lessonDate = lessonDate,
                lessonTitle = lessonTitle.trim(),
                surahName = surahName.trim(),
                ayahNumber = ayahNumber.trim(),
                homework = homeworkDesc.trim(),
                teacherNotes = teacherNotes.trim(),
                createdBy = creator
            )
            repository.insertLesson(lesson)

            // If homework is specified and we have active students, auto-assign this homework to all active students!
            if (homeworkDesc.trim().isNotEmpty()) {
                val studentsList = repository.allStudents.firstOrNull() ?: emptyList()
                studentsList.filter { it.status == "Active" }.forEach { student ->
                    repository.insertHomework(
                        Homework(
                            studentId = student.studentId,
                            title = "Homework: $lessonTitle",
                            description = homeworkDesc.trim(),
                            assignedDate = lessonDate,
                            submissionDate = getFutureDate(lessonDate, 3), // due in 3 days
                            status = "Pending"
                        )
                    )
                }
            }

            // Also increment lessons completed count for active students
            val studentsList = repository.allStudents.firstOrNull() ?: emptyList()
            studentsList.filter { it.status == "Active" }.forEach { student ->
                val updatedStudent = student.copy(
                    totalLessonsCompleted = student.totalLessonsCompleted + 1,
                    currentSurah = surahName.trim(),
                    currentAyah = ayahNumber.trim(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateStudent(updatedStudent)
            }

            onComplete()
        }
    }

    private fun getFutureDate(currentDate: String, daysToAdd: Int): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(currentDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            return sdf.format(cal.time)
        } catch (e: Exception) {
            return currentDate
        }
    }

    fun deleteLesson(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteLessonById(id)
            onComplete()
        }
    }

    // Save Homework Feedback or mark Completed
    fun updateHomeworkStatus(homeworkId: Int, status: String, feedback: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val homeworkList = repository.allHomework.firstOrNull() ?: emptyList()
            val existing = homeworkList.find { it.id == homeworkId }
            if (existing != null) {
                val updated = existing.copy(
                    status = status,
                    teacherFeedback = feedback.trim()
                )
                repository.insertHomework(updated)

                // If marked completed, update student homework counts
                if (status == "Completed") {
                    val student = repository.getStudentById(existing.studentId)
                    if (student != null) {
                        repository.updateStudent(
                            student.copy(
                                homeworkCompleted = student.homeworkCompleted + 1,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                onComplete()
            }
        }
    }

    fun studentSubmitHomework(homeworkId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val homeworkList = repository.allHomework.firstOrNull() ?: emptyList()
            val existing = homeworkList.find { it.id == homeworkId }
            if (existing != null) {
                val updated = existing.copy(status = "Submitted")
                repository.insertHomework(updated)
                onComplete()
            }
        }
    }

    // Save Test Result
    fun addTest(studentId: Int, testTitle: String, date: String, marksObtained: Double, totalMarks: Double, remarks: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (testTitle.trim().isEmpty()) {
            onError("Test Title cannot be empty.")
            return
        }
        if (totalMarks <= 0) {
            onError("Total Marks must be greater than 0.")
            return
        }
        if (marksObtained > totalMarks) {
            onError("Marks Obtained cannot exceed Total Marks.")
            return
        }

        viewModelScope.launch {
            val percentage = (marksObtained / totalMarks) * 100.0
            val test = Test(
                studentId = studentId,
                testTitle = testTitle.trim(),
                date = date,
                marksObtained = marksObtained,
                totalMarks = totalMarks,
                percentage = percentage,
                remarks = remarks.trim()
            )
            repository.insertTest(test)

            // Increment student test completed count
            val student = repository.getStudentById(studentId)
            if (student != null) {
                repository.updateStudent(
                    student.copy(
                        testsCompleted = student.testsCompleted + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteTest(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTestById(id)
            onComplete()
        }
    }

    // Update daily Tafseer Lesson
    fun updateLesson(lesson: Lesson, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (lesson.lessonTitle.trim().isEmpty()) {
            onError("Lesson title cannot be empty.")
            return
        }
        if (lesson.surahName.trim().isEmpty()) {
            onError("Surah Name cannot be empty.")
            return
        }
        if (lesson.ayahNumber.trim().isEmpty()) {
            onError("Ayah Number cannot be empty.")
            return
        }
        viewModelScope.launch {
            repository.insertLesson(lesson)
            onComplete()
        }
    }

    // Add Homework directly
    fun addHomework(studentId: Int, title: String, description: String, assignedDate: String, submissionDate: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (title.trim().isEmpty()) {
            onError("Homework Title cannot be empty.")
            return
        }
        if (description.trim().isEmpty()) {
            onError("Homework details cannot be empty.")
            return
        }
        viewModelScope.launch {
            val homework = Homework(
                studentId = studentId,
                title = title.trim(),
                description = description.trim(),
                assignedDate = assignedDate,
                submissionDate = submissionDate,
                status = "Pending"
            )
            repository.insertHomework(homework)
            onComplete()
        }
    }

    // Update Homework details (edit mode)
    fun updateHomeworkDetails(homework: Homework, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (homework.title.trim().isEmpty()) {
            onError("Homework Title cannot be empty.")
            return
        }
        if (homework.description.trim().isEmpty()) {
            onError("Homework details cannot be empty.")
            return
        }
        viewModelScope.launch {
            repository.insertHomework(homework)
            onComplete()
        }
    }

    // Delete Homework
    fun deleteHomework(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHomeworkById(id)
            onComplete()
        }
    }

    // Update Test Details
    fun updateTest(test: Test, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (test.testTitle.trim().isEmpty()) {
            onError("Test Title cannot be empty.")
            return
        }
        if (test.totalMarks <= 0) {
            onError("Total Marks must be greater than 0.")
            return
        }
        if (test.marksObtained > test.totalMarks) {
            onError("Marks Obtained cannot exceed Total Marks.")
            return
        }
        viewModelScope.launch {
            val percentage = (test.marksObtained / test.totalMarks) * 100.0
            val updated = test.copy(percentage = percentage)
            repository.insertTest(updated)
            onComplete()
        }
    }

    // Add Important Note
    fun addImportantNote(title: String, note: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (title.trim().isEmpty()) {
            onError("Note Title cannot be empty.")
            return
        }
        if (note.trim().isEmpty()) {
            onError("Note details cannot be empty.")
            return
        }

        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val creator = _currentUser.value?.name ?: "Bint e Khalid"
            val importantNote = ImportantNote(
                title = title.trim(),
                note = note.trim(),
                createdDate = dateStr,
                createdBy = creator
            )
            repository.insertNote(importantNote)
            onComplete()
        }
    }

    fun deleteNote(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
            onComplete()
        }
    }
}
