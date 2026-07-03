package com.example.data.repository

import android.content.Context
import com.example.data.dao.*
import com.example.data.model.*
import com.example.data.sync.FirebaseSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(
    private val userDao: UserDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val lessonDao: LessonDao,
    private val homeworkDao: HomeworkDao,
    private val testDao: TestDao,
    private val importantNoteDao: ImportantNoteDao,
    private val islamicContentDao: IslamicContentDao
) {
    private var syncManager: FirebaseSyncManager? = null

    fun initSync(context: Context) {
        syncManager = FirebaseSyncManager.getInstance(context)
    }

    // Islamic Content functions
    suspend fun getAllIslamicContentSortedByLastDisplayed(): List<IslamicContent> = 
        islamicContentDao.getAllContentSortedByLastDisplayed()

    suspend fun getAllIslamicContent(): List<IslamicContent> = 
        islamicContentDao.getAllContent()

    suspend fun insertIslamicContent(content: IslamicContent): Long = 
        islamicContentDao.insertContent(content)

    suspend fun updateIslamicContent(content: IslamicContent) = 
        islamicContentDao.updateContent(content)

    suspend fun deleteIslamicContentById(id: Int) = 
        islamicContentDao.deleteContentById(id)

    // User functions
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getUserFlow(uid: String): Flow<User?> = userDao.getUserFlow(uid)
    suspend fun getUserByUid(uid: String): User? = userDao.getUserByUid(uid)
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
        syncManager?.uploadUser(user)
    }

    private fun generateUniqueIntId(): Int {
        return java.util.UUID.randomUUID().hashCode() and Int.MAX_VALUE
    }

    // Student functions
    val allStudents: Flow<List<Student>> = studentDao.getAllStudentsFlow()
    suspend fun getAllStudents(): List<Student> = studentDao.getAllStudents()
    suspend fun getStudentById(id: Int): Student? = studentDao.getStudentById(id)
    fun getStudentFlowById(id: Int): Flow<Student?> = studentDao.getStudentFlowById(id)
    suspend fun insertStudent(student: Student): Long {
        val studentWithId = if (student.studentId == 0) {
            student.copy(studentId = generateUniqueIntId())
        } else {
            student
        }
        studentDao.insertStudent(studentWithId)
        syncManager?.uploadStudent(studentWithId)
        return studentWithId.studentId.toLong()
    }
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
        syncManager?.uploadStudent(student)
    }
    suspend fun deleteStudentById(id: Int) {
        studentDao.deleteStudentById(id)
        syncManager?.deleteStudent(id)
    }

    // Attendance functions
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendanceFlow()
    fun getAttendanceByDateFlow(date: String): Flow<List<Attendance>> = attendanceDao.getAttendanceByDateFlow(date)
    suspend fun getAttendanceByDate(date: String): List<Attendance> = attendanceDao.getAttendanceByDate(date)
    fun getAttendanceForStudentFlow(studentId: Int): Flow<List<Attendance>> = attendanceDao.getAttendanceForStudentFlow(studentId)
    suspend fun insertAttendance(attendance: Attendance) {
        val attendanceWithId = if (attendance.id == 0) {
            attendance.copy(id = generateUniqueIntId())
        } else {
            attendance
        }
        attendanceDao.insertAttendance(attendanceWithId)
        syncManager?.uploadAttendance(attendanceWithId)
    }
    suspend fun insertAllAttendance(attendanceList: List<Attendance>) {
        val listWithIds = attendanceList.map { att ->
            if (att.id == 0) att.copy(id = generateUniqueIntId()) else att
        }
        attendanceDao.insertAllAttendance(listWithIds)
        for (att in listWithIds) {
            syncManager?.uploadAttendance(att)
        }
    }
    suspend fun deleteAttendanceForStudentOnDate(studentId: Int, date: String) {
        val list = attendanceDao.getAttendanceByDate(date)
        val target = list.firstOrNull { it.studentId == studentId }
        attendanceDao.deleteAttendanceForStudentOnDate(studentId, date)
        if (target != null) {
            syncManager?.deleteAttendance(target.id)
        }
    }
    suspend fun deleteAttendanceById(id: Int) {
        attendanceDao.deleteAttendanceById(id)
        syncManager?.deleteAttendance(id)
    }
    suspend fun deleteAttendanceByDate(date: String) {
        val list = attendanceDao.getAttendanceByDate(date)
        attendanceDao.deleteAttendanceByDate(date)
        for (att in list) {
            syncManager?.deleteAttendance(att.id)
        }
    }

    // Lesson functions
    val allLessons: Flow<List<Lesson>> = lessonDao.getAllLessonsFlow()
    val recentLessons: Flow<List<Lesson>> = lessonDao.getRecentLessonsFlow()
    suspend fun insertLesson(lesson: Lesson) {
        val lessonWithId = if (lesson.id == 0) {
            lesson.copy(id = generateUniqueIntId())
        } else {
            lesson
        }
        lessonDao.insertLesson(lessonWithId)
        syncManager?.uploadLesson(lessonWithId)
    }
    suspend fun deleteLessonById(id: Int) {
        lessonDao.deleteLessonById(id)
        syncManager?.deleteLesson(id)
    }

    // Homework functions
    val allHomework: Flow<List<Homework>> = homeworkDao.getAllHomeworkFlow()
    fun getHomeworkForStudentFlow(studentId: Int): Flow<List<Homework>> = homeworkDao.getHomeworkForStudentFlow(studentId)
    suspend fun insertHomework(homework: Homework) {
        val homeworkWithId = if (homework.id == 0) {
            homework.copy(id = generateUniqueIntId())
        } else {
            homework
        }
        homeworkDao.insertHomework(homeworkWithId)
        syncManager?.uploadHomework(homeworkWithId)
    }
    suspend fun deleteHomeworkById(id: Int) {
        homeworkDao.deleteHomeworkById(id)
        syncManager?.deleteHomework(id)
    }

    // Test functions
    val allTests: Flow<List<Test>> = testDao.getAllTestsFlow()
    fun getTestsForStudentFlow(studentId: Int): Flow<List<Test>> = testDao.getTestsForStudentFlow(studentId)
    suspend fun insertTest(test: Test) {
        val testWithId = if (test.id == 0) {
            test.copy(id = generateUniqueIntId())
        } else {
            test
        }
        testDao.insertTest(testWithId)
        syncManager?.uploadTest(testWithId)
    }
    suspend fun deleteTestById(id: Int) {
        testDao.deleteTestById(id)
        syncManager?.deleteTest(id)
    }

    // Important Note functions
    val allNotes: Flow<List<ImportantNote>> = importantNoteDao.getAllNotesFlow()
    suspend fun insertNote(note: ImportantNote) {
        val noteWithId = if (note.id == 0) {
            note.copy(id = generateUniqueIntId())
        } else {
            note
        }
        importantNoteDao.insertNote(noteWithId)
        syncManager?.uploadNote(noteWithId)
    }
    suspend fun deleteNoteById(id: Int) {
        importantNoteDao.deleteNoteById(id)
        syncManager?.deleteNote(id)
    }

    // Pre-populate data helper
    suspend fun prepopulateIfEmpty() {
        // Check if students empty
        val studentsList = studentDao.getAllStudentsFlow().firstOrNull() ?: emptyList()
        val names = listOf(
            "Iqra Afzal", "Wajiha", "Ambreen", "Zubaira", "Noushba Sami",
            "Nida", "Saeeda", "Rashida", "Sonia", "Zainab Zaka",
            "Tanees", "Aneela", "Iqra Ikram", "Noor Afzal", "Pakeeza",
            "Noushba Shabbir", "Aliya", "Nosheen", "Saba", "Samra",
            "Nazish", "Ali Hassan", "Fatima Noor", "Ayesha Khan"
        )
        if (studentsList.isEmpty()) {
            for (name in names) {
                studentDao.insertStudent(
                    Student(
                        fullName = name,
                        status = "Active",
                        classesAttended = 0,
                        classesMissed = 0,
                        attendancePercentage = 0,
                        currentSurah = "Al-Baqarah",
                        currentAyah = "25",
                        generalNotes = "Excellent progress in recitation."
                    )
                )
            }
        }

        // Check if users empty
        val existingUser = userDao.getUserByEmail("teacher@healing.com")
        if (existingUser == null) {
            // Populate roles
            userDao.insertUser(User("uid_teacher", "Bint e Khalid", "teacher@healing.com", "Teacher"))
            userDao.insertUser(User("uid_helper", "Nida", "helper@healing.com", "Helper"))
        }

        // Ensure default student@healing.com user exists for compatibility
        val defaultStudentUser = userDao.getUserByEmail("student@healing.com")
        if (defaultStudentUser == null) {
            userDao.insertUser(User("uid_student", "Iqra Afzal", "student@healing.com", "Student"))
        }

        // Ensure every student in the student database has a corresponding User record for independent login
        val allCurrentStudents = studentDao.getAllStudents()
        for (student in allCurrentStudents) {
            val cleanEmail = student.fullName.lowercase().replace(" ", ".") + "@healing.com"
            val existingStudentUser = userDao.getUserByEmail(cleanEmail)
            if (existingStudentUser == null) {
                val studentUid = "uid_student_" + student.fullName.lowercase().replace(" ", "_")
                userDao.insertUser(User(studentUid, student.fullName, cleanEmail, "Student"))
            }
        }

        // Prepopulate Islamic Content database table if empty
        val existingContent = islamicContentDao.getAllContent()
        if (existingContent.isEmpty()) {
            val seedList = com.example.utils.DailyContentProvider.allDynamicContent.map { dynamicItem ->
                IslamicContent(
                    type = dynamicItem.type.name, // "AYAH", "HADITH", "DUA"
                    arabicText = dynamicItem.arabicText,
                    urduTranslation = dynamicItem.urduTranslation,
                    englishTranslation = dynamicItem.englishTranslation,
                    surahName = dynamicItem.surahName,
                    ayahNumber = dynamicItem.ayahNumber,
                    reference = dynamicItem.reference,
                    bookName = dynamicItem.bookName,
                    source = dynamicItem.source,
                    lastDisplayedAt = 0L
                )
            }
            islamicContentDao.insertAllContent(seedList)
        }
    }
}
