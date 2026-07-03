package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserFlow(uid: String): Flow<User?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY fullName ASC")
    suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM students WHERE studentId = :id LIMIT 1")
    suspend fun getStudentById(id: Int): Student?

    @Query("SELECT * FROM students WHERE studentId = :id LIMIT 1")
    fun getStudentFlowById(id: Int): Flow<Student?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("DELETE FROM students WHERE studentId = :id")
    suspend fun deleteStudentById(id: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDateFlow(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceByDate(date: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudentFlow(studentId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(attendanceList: List<Attendance>): List<Long>

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendanceForStudentOnDate(studentId: Int, date: String)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Int)

    @Query("DELETE FROM attendance WHERE date = :date")
    suspend fun deleteAttendanceByDate(date: String)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY lessonDate DESC, id DESC")
    fun getAllLessonsFlow(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons ORDER BY lessonDate DESC, id DESC LIMIT 5")
    fun getRecentLessonsFlow(): Flow<List<Lesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson): Long

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Int)
}

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework ORDER BY assignedDate DESC, id DESC")
    fun getAllHomeworkFlow(): Flow<List<Homework>>

    @Query("SELECT * FROM homework WHERE studentId = :studentId ORDER BY assignedDate DESC, id DESC")
    fun getHomeworkForStudentFlow(studentId: Int): Flow<List<Homework>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: Homework): Long

    @Query("DELETE FROM homework WHERE id = :id")
    suspend fun deleteHomeworkById(id: Int)
}

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY date DESC, id DESC")
    fun getAllTestsFlow(): Flow<List<Test>>

    @Query("SELECT * FROM tests WHERE studentId = :studentId ORDER BY date DESC, id DESC")
    fun getTestsForStudentFlow(studentId: Int): Flow<List<Test>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: Test): Long

    @Query("DELETE FROM tests WHERE id = :id")
    suspend fun deleteTestById(id: Int)
}

@Dao
interface ImportantNoteDao {
    @Query("SELECT * FROM important_notes ORDER BY createdDate DESC, id DESC")
    fun getAllNotesFlow(): Flow<List<ImportantNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ImportantNote): Long

    @Query("DELETE FROM important_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface IslamicContentDao {
    @Query("SELECT * FROM islamic_content ORDER BY lastDisplayedAt ASC")
    suspend fun getAllContentSortedByLastDisplayed(): List<IslamicContent>

    @Query("SELECT * FROM islamic_content")
    suspend fun getAllContent(): List<IslamicContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: IslamicContent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContent(contents: List<IslamicContent>)

    @Update
    suspend fun updateContent(content: IslamicContent)

    @Query("DELETE FROM islamic_content WHERE id = :id")
    suspend fun deleteContentById(id: Int)
}
