package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseSyncManager private constructor(context: Context) {

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncStatus = MutableStateFlow("Synced") // "Synced", "Syncing", "Offline", "Error"
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        try {
            com.google.firebase.FirebaseApp.getInstance()
            Log.d("FirebaseSyncManager", "FirebaseApp already initialized (probably via google-services.json).")
        } catch (e: IllegalStateException) {
            try {
                val apiKey = if (com.example.BuildConfig.FIREBASE_API_KEY.isNotEmpty() && com.example.BuildConfig.FIREBASE_API_KEY != "dummy-api-key") {
                    com.example.BuildConfig.FIREBASE_API_KEY
                } else {
                    "dummy-api-key"
                }

                val projectId = if (com.example.BuildConfig.FIREBASE_PROJECT_ID.isNotEmpty() && com.example.BuildConfig.FIREBASE_PROJECT_ID != "dummy-project-id") {
                    com.example.BuildConfig.FIREBASE_PROJECT_ID
                } else {
                    "dummy-project-id"
                }

                val appId = if (com.example.BuildConfig.FIREBASE_APPLICATION_ID.isNotEmpty() && com.example.BuildConfig.FIREBASE_APPLICATION_ID != "1:1234567890:android:1234567890abcdef") {
                    com.example.BuildConfig.FIREBASE_APPLICATION_ID
                } else {
                    "1:1234567890:android:1234567890abcdef"
                }

                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .setApiKey(apiKey)
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d("FirebaseSyncManager", "FirebaseApp initialized programmatically. API Key: ${if (apiKey == "dummy-api-key") "dummy" else "valid"}")
            } catch (ex: Exception) {
                Log.e("FirebaseSyncManager", "Failed to initialize FirebaseApp programmatically", ex)
            }
        }

        // Initialize connection status using ConnectivityManager
        val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager != null) {
            val builder = android.net.NetworkRequest.Builder()
            builder.addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            // Initial network capability check
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val hasInternet = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isOnline.value = hasInternet
            _syncStatus.value = if (hasInternet) "Synced" else "Offline"

            try {
                connectivityManager.registerNetworkCallback(builder.build(), object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        _isOnline.value = true
                        _syncStatus.value = "Synced"
                        Log.d(TAG, "Network connection restored. Sync online.")
                    }

                    override fun onLost(network: android.net.Network) {
                        _isOnline.value = false
                        _syncStatus.value = "Offline"
                        Log.d(TAG, "Network connection lost. Offline mode.")
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register network callback", e)
            }
        }
    }

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val appDb = AppDatabase.getDatabase(context.applicationContext)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "FirebaseSyncManager"
        @Volatile
        private var INSTANCE: FirebaseSyncManager? = null

        fun getInstance(context: Context): FirebaseSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseSyncManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    fun startSyncing() {
        Log.d(TAG, "Starting real-time Firebase sync...")
        syncUsers()
        syncStudents()
        syncAttendance()
        syncLessons()
        syncHomework()
        syncTests()
        syncImportantNotes()
    }

    private fun syncUsers() {
        db.collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for users", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(User::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.userDao().insertUser(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        // Normally we don't delete users, but sync it if it happens
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in users sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    private fun syncStudents() {
        db.collection("students")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for students", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(Student::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.studentDao().insertStudent(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        appDb.studentDao().deleteStudentById(item.studentId)
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in students sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    private fun syncAttendance() {
        db.collection("attendance")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for attendance", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(Attendance::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.attendanceDao().insertAttendance(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        appDb.attendanceDao().deleteAttendanceById(item.id)
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in attendance sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    private fun syncLessons() {
        db.collection("lessons")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for lessons", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(Lesson::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.lessonDao().insertLesson(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        appDb.lessonDao().deleteLessonById(item.id)
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in lessons sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    private fun syncHomework() {
        db.collection("homework")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for homework", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(Homework::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.homeworkDao().insertHomework(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        appDb.homeworkDao().deleteHomeworkById(item.id)
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in homework sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    private fun syncTests() {
        db.collection("tests")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for tests", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(Test::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.testDao().insertTest(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        appDb.testDao().deleteTestById(item.id)
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in tests sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    private fun syncImportantNotes() {
        db.collection("important_notes")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for important_notes", error)
                    _syncStatus.value = "Error"
                    return@addSnapshotListener
                }
                snapshots?.let { querySnapshot ->
                    if (querySnapshot.documentChanges.isNotEmpty()) {
                        _syncStatus.value = "Syncing"
                    }
                    coroutineScope.launch {
                        try {
                            for (change in querySnapshot.documentChanges) {
                                val item = change.document.toObject(ImportantNote::class.java)
                                when (change.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        appDb.importantNoteDao().insertNote(item)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        appDb.importantNoteDao().deleteNoteById(item.id)
                                    }
                                }
                            }
                            if (querySnapshot.documentChanges.isNotEmpty()) {
                                kotlinx.coroutines.delay(800)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in notes sync task", e)
                        } finally {
                            if (_isOnline.value) _syncStatus.value = "Synced"
                        }
                    }
                }
            }
    }

    // Write functions
    fun uploadUser(user: User) {
        try {
            db.collection("users").document(user.uid).set(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading user to Firestore", e)
        }
    }

    fun uploadStudent(student: Student) {
        try {
            db.collection("students").document(student.studentId.toString()).set(student)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading student to Firestore", e)
        }
    }

    fun deleteStudent(studentId: Int) {
        try {
            db.collection("students").document(studentId.toString()).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting student from Firestore", e)
        }
    }

    fun uploadAttendance(attendance: Attendance) {
        try {
            db.collection("attendance").document(attendance.id.toString()).set(attendance)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading attendance to Firestore", e)
        }
    }

    fun deleteAttendance(id: Int) {
        try {
            db.collection("attendance").document(id.toString()).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting attendance from Firestore", e)
        }
    }

    fun uploadLesson(lesson: Lesson) {
        try {
            db.collection("lessons").document(lesson.id.toString()).set(lesson)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading lesson to Firestore", e)
        }
    }

    fun deleteLesson(id: Int) {
        try {
            db.collection("lessons").document(id.toString()).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting lesson from Firestore", e)
        }
    }

    fun uploadHomework(homework: Homework) {
        try {
            db.collection("homework").document(homework.id.toString()).set(homework)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading homework to Firestore", e)
        }
    }

    fun deleteHomework(id: Int) {
        try {
            db.collection("homework").document(id.toString()).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting homework from Firestore", e)
        }
    }

    fun uploadTest(test: Test) {
        try {
            db.collection("tests").document(test.id.toString()).set(test)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading test to Firestore", e)
        }
    }

    fun deleteTest(id: Int) {
        try {
            db.collection("tests").document(id.toString()).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting test from Firestore", e)
        }
    }

    fun uploadNote(note: ImportantNote) {
        try {
            db.collection("important_notes").document(note.id.toString()).set(note)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading note to Firestore", e)
        }
    }

    fun deleteNote(id: Int) {
        try {
            db.collection("important_notes").document(id.toString()).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note from Firestore", e)
        }
    }
}
