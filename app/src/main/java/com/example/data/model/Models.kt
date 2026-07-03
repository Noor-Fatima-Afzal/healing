package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "Teacher", "Helper", "Student"
    val profilePhoto: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val studentId: Int = 0,
    val fullName: String = "",
    val status: String = "Active", // "Active", "Inactive"
    val classesAttended: Int = 0,
    val classesMissed: Int = 0,
    val attendancePercentage: Int = 100,
    val currentSurah: String = "Al-Baqarah",
    val currentAyah: String = "1",
    val totalLessonsCompleted: Int = 0,
    val homeworkCompleted: Int = 0,
    val testsCompleted: Int = 0,
    val generalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int = 0,
    val studentName: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val day: String = "", // "Saturday", "Monday", etc.
    val time: String = "17:30",
    val status: String = "Present", // "Present", "Absent", "Leave"
    val recordedBy: String = "", // Teacher or Helper name
    val teacherNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lessonDate: String = "", // "YYYY-MM-DD"
    val lessonTitle: String = "",
    val surahName: String = "",
    val ayahNumber: String = "",
    val homework: String = "",
    val teacherNotes: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "homework")
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int = 0,
    val title: String = "",
    val description: String = "",
    val assignedDate: String = "",
    val submissionDate: String = "",
    val status: String = "Pending", // "Pending", "Submitted", "Completed"
    val teacherFeedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tests")
data class Test(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int = 0,
    val testTitle: String = "",
    val date: String = "",
    val marksObtained: Double = 0.0,
    val totalMarks: Double = 100.0,
    val percentage: Double = 0.0, // Auto-calculated: (marksObtained / totalMarks) * 100
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "important_notes")
data class ImportantNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val note: String = "",
    val createdDate: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "islamic_content")
data class IslamicContent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "AYAH", "HADITH", "DUA"
    val arabicText: String = "",
    val urduTranslation: String = "",
    val englishTranslation: String = "",
    val surahName: String = "",
    val ayahNumber: String = "",
    val reference: String = "",
    val bookName: String = "",
    val source: String = "",
    val lastDisplayedAt: Long = 0
)
