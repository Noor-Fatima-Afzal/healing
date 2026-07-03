package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    viewModel: MainViewModel,
    currentUser: User
) {
    val context = LocalContext.current
    val lessons by viewModel.allLessons.collectAsState()
    val homeworkList by viewModel.allHomework.collectAsState()
    val testsList by viewModel.allTests.collectAsState()
    val notesList by viewModel.allNotes.collectAsState()
    val students by viewModel.allStudents.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Lessons, 1: Homework, 2: Notes, 3: Tests

    val tabTitles = listOf("Lessons", "Homework", "Notes", "Tests")
    val tabIcons = listOf(Icons.Default.MenuBook, Icons.Default.Assignment, Icons.Default.Campaign, Icons.Default.Grading)

    val isStudentRole = currentUser.role == "Student"

    // Dialog & Add States
    var showAddLessonDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddTestDialog by remember { mutableStateOf(false) }
    var showAddHomeworkDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf<Homework?>(null) }

    var showEditLessonDialog by remember { mutableStateOf<Lesson?>(null) }
    var showEditHomeworkDialog by remember { mutableStateOf<Homework?>(null) }
    var showEditTestDialog by remember { mutableStateOf<Test?>(null) }

    Scaffold(
        containerColor = WarmWhite,
        floatingActionButton = {
            val canManage = currentUser.role == "Teacher" || currentUser.role == "Helper" || currentUser.role == "Admin"
            if (canManage) {
                FloatingActionButton(
                    onClick = {
                        when (activeTab) {
                            0 -> showAddLessonDialog = true
                            1 -> showAddHomeworkDialog = true
                            2 -> showAddNoteDialog = true
                            3 -> showAddTestDialog = true
                        }
                    },
                    containerColor = EmeraldGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Content")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal scrollable Tab bar
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = EmeraldGreen,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) },
                        icon = { Icon(tabIcons[index], contentDescription = title) },
                        selectedContentColor = EmeraldGreen,
                        unselectedContentColor = Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // LESSONS HISTORY TIMELINE
                        if (lessons.isEmpty()) {
                            EmptyStatePlaceholder(text = "No lessons recorded yet. Tap + to record first class.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(lessons) { lesson ->
                                    val canManageLesson = currentUser.role == "Teacher" || currentUser.role == "Helper" || currentUser.role == "Admin"
                                    LessonHistoryCard(
                                        lesson = lesson,
                                        canManage = canManageLesson,
                                        onDelete = {
                                            viewModel.deleteLesson(lesson.id) {
                                                Toast.makeText(context, "Lesson removed.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onEdit = {
                                            showEditLessonDialog = lesson
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }

                    1 -> {
                        // HOMEWORK MANAGER
                        val hwToDisplay = if (isStudentRole) {
                            homeworkList.filter { hw ->
                                val s = students.find { it.studentId == hw.studentId }
                                s?.fullName?.contains(currentUser.name, ignoreCase = true) == true
                            }
                        } else {
                            homeworkList
                        }

                        if (hwToDisplay.isEmpty()) {
                            EmptyStatePlaceholder(text = "No homework assigned yet. Tap + to assign some.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(hwToDisplay) { hw ->
                                    val studentName = students.find { it.studentId == hw.studentId }?.fullName ?: "Student"
                                    HomeworkCard(
                                        homework = hw,
                                        studentName = studentName,
                                        isStudentRole = isStudentRole,
                                        onStatusChange = { newStatus ->
                                            if (isStudentRole && newStatus == "Submitted") {
                                                viewModel.studentSubmitHomework(hw.id) {
                                                    Toast.makeText(context, "Homework submitted to teacher.", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                // Teacher marking completed / giving feedback
                                                showFeedbackDialog = hw
                                            }
                                        },
                                        onEdit = {
                                            showEditHomeworkDialog = hw
                                        },
                                        onDelete = {
                                            viewModel.deleteHomework(hw.id) {
                                                Toast.makeText(context, "Homework removed.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }

                    2 -> {
                        // IMPORTANT NOTES
                        if (notesList.isEmpty()) {
                            EmptyStatePlaceholder(text = "No announcements or revision guidelines recorded yet.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(notesList) { note ->
                                    val canDeleteNote = currentUser.role == "Teacher" || currentUser.role == "Helper" || currentUser.role == "Admin"
                                    ImportantNoteCard(note = note, canDelete = canDeleteNote) {
                                        viewModel.deleteNote(note.id) {
                                            Toast.makeText(context, "Note removed.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }

                    3 -> {
                        // TESTS TRACKER
                        val testsToDisplay = if (isStudentRole) {
                            testsList.filter { t ->
                                val s = students.find { it.studentId == t.studentId }
                                s?.fullName?.contains(currentUser.name, ignoreCase = true) == true
                            }
                        } else {
                            testsList
                        }

                        if (testsToDisplay.isEmpty()) {
                            EmptyStatePlaceholder(text = "No test records added yet.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(testsToDisplay) { test ->
                                    val studentName = students.find { it.studentId == test.studentId }?.fullName ?: "Student"
                                    val canManageTest = currentUser.role == "Teacher" || currentUser.role == "Helper" || currentUser.role == "Admin"
                                    TestCard(
                                        test = test,
                                        studentName = studentName,
                                        canManage = canManageTest,
                                        onDelete = {
                                            viewModel.deleteTest(test.id) {
                                                Toast.makeText(context, "Test record deleted.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onEdit = {
                                            showEditTestDialog = test
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }

            // ADD DAILY LESSON DIALOG
            if (showAddLessonDialog) {
                var lessonTitle by remember { mutableStateOf("") }
                var surahName by remember { mutableStateOf("") }
                var ayahNumber by remember { mutableStateOf("") }
                var homeworkDesc by remember { mutableStateOf("") }
                var notes by remember { mutableStateOf("") }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var lessonDate by remember { mutableStateOf(sdf.format(Date())) }

                val calendarInstance = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        lessonDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                AlertDialog(
                    onDismissRequest = { showAddLessonDialog = false },
                    title = { Text("Record Daily Tafseer Lesson", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Date row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lesson Date: $lessonDate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Button(
                                    onClick = { datePicker.show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f))
                                ) {
                                    Text("Change Date", color = EmeraldGreen, fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = lessonTitle,
                                onValueChange = { lessonTitle = it },
                                label = { Text("Lesson Title (e.g., Virtues of Ayah al-Kursi)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = surahName,
                                    onValueChange = { surahName = it },
                                    label = { Text("Surah Name") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                                OutlinedTextField(
                                    value = ayahNumber,
                                    onValueChange = { ayahNumber = it },
                                    label = { Text("Ayah(s)") },
                                    modifier = Modifier.weight(0.7f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                            }

                            OutlinedTextField(
                                value = homeworkDesc,
                                onValueChange = { homeworkDesc = it },
                                label = { Text("Homework Assigned (Optional)") },
                                placeholder = { Text("Homework description will auto-assign to active students...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Teacher Reflection Notes") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.addLesson(lessonDate, lessonTitle, surahName, ayahNumber, homeworkDesc, notes, {
                                    Toast.makeText(context, "Lesson saved. Homework assigned.", Toast.LENGTH_SHORT).show()
                                    showAddLessonDialog = false
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Save Lesson", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddLessonDialog = false }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // ADD IMPORTANT NOTE DIALOG
            if (showAddNoteDialog) {
                var title by remember { mutableStateOf("") }
                var noteBody by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddNoteDialog = false },
                    title = { Text("Add Important Note/Announcement", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Note Title (e.g., Revision Guidelines)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            OutlinedTextField(
                                value = noteBody,
                                onValueChange = { noteBody = it },
                                label = { Text("Note Details") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.addImportantNote(title, noteBody, {
                                    Toast.makeText(context, "Note published.", Toast.LENGTH_SHORT).show()
                                    showAddNoteDialog = false
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddNoteDialog = false }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // ADD TEST SCORE DIALOG
            if (showAddTestDialog) {
                val activeStudents = students.filter { it.status == "Active" }
                var selectedStudentIndex by remember { mutableStateOf(0) }
                var testTitle by remember { mutableStateOf("") }
                var marksObtained by remember { mutableStateOf("") }
                var totalMarks by remember { mutableStateOf("100") }
                var remarks by remember { mutableStateOf("") }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var testDate by remember { mutableStateOf(sdf.format(Date())) }

                val calendarInstance = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        testDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                var expandedDropdown by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showAddTestDialog = false },
                    title = { Text("Record Test Result", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        if (activeStudents.isEmpty()) {
                            Text("Please add active students before grading.", color = ErrorRed)
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Student Selector Dropdown
                                Column {
                                    Text("Select Student:", style = MaterialTheme.typography.labelSmall, color = Gray)
                                    Box {
                                        Button(
                                            onClick = { expandedDropdown = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = LightGray.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(activeStudents[selectedStudentIndex].fullName, color = DarkGray)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.ArrowDropDown, "Select", tint = DarkGray)
                                        }

                                        DropdownMenu(
                                            expanded = expandedDropdown,
                                            onDismissRequest = { expandedDropdown = false }
                                        ) {
                                            activeStudents.forEachIndexed { i, student ->
                                                DropdownMenuItem(
                                                    text = { Text(student.fullName) },
                                                    onClick = {
                                                        selectedStudentIndex = i
                                                        expandedDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Test Date: $testDate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Button(
                                        onClick = { datePicker.show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f))
                                    ) {
                                        Text("Change Date", color = EmeraldGreen, fontSize = 12.sp)
                                    }
                                }

                                OutlinedTextField(
                                    value = testTitle,
                                    onValueChange = { testTitle = it },
                                    label = { Text("Test Title (e.g., Surah Al-Baqarah 1-50 Oral)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = marksObtained,
                                        onValueChange = { marksObtained = it },
                                        label = { Text("Marks Obtained") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                    )
                                    OutlinedTextField(
                                        value = totalMarks,
                                        onValueChange = { totalMarks = it },
                                        label = { Text("Total Marks") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                    )
                                }

                                OutlinedTextField(
                                    value = remarks,
                                    onValueChange = { remarks = it },
                                    label = { Text("Teacher Remarks / Feedback") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (activeStudents.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val student = activeStudents[selectedStudentIndex]
                                    val obtained = marksObtained.toDoubleOrNull() ?: -1.0
                                    val total = totalMarks.toDoubleOrNull() ?: -1.0

                                    viewModel.addTest(student.studentId, testTitle, testDate, obtained, total, remarks, {
                                        Toast.makeText(context, "Test score graded successfully.", Toast.LENGTH_SHORT).show()
                                        showAddTestDialog = false
                                    }, { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    })
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("Submit Grade", color = Color.White)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddTestDialog = false }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // FEEDBACK DIALOG (Teacher marking homework completed and giving feedback)
            if (showFeedbackDialog != null) {
                val hw = showFeedbackDialog!!
                var remarksFeedback by remember { mutableStateOf("") }
                var markCompleted by remember { mutableStateOf(true) }

                AlertDialog(
                    onDismissRequest = { showFeedbackDialog = null },
                    title = { Text("Homework Review & Feedback", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reviewing homework assigned on ${hw.assignedDate}.", style = MaterialTheme.typography.bodySmall, color = Gray)
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { markCompleted = !markCompleted }
                            ) {
                                Checkbox(
                                    checked = markCompleted,
                                    onCheckedChange = { markCompleted = it },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                                )
                                Text("Mark as Completed")
                            }

                            OutlinedTextField(
                                value = remarksFeedback,
                                onValueChange = { remarksFeedback = it },
                                label = { Text("Teacher Feedback / Remarks") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val finalStatus = if (markCompleted) "Completed" else "Submitted"
                                viewModel.updateHomeworkStatus(hw.id, finalStatus, remarksFeedback) {
                                    Toast.makeText(context, "Homework feedback updated.", Toast.LENGTH_SHORT).show()
                                    showFeedbackDialog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Publish Feedback", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFeedbackDialog = null }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // ADD HOMEWORK DIALOG
            if (showAddHomeworkDialog) {
                val activeStudents = students.filter { it.status == "Active" }
                var selectedStudentIndex by remember { mutableStateOf(0) }
                var hwTitle by remember { mutableStateOf("") }
                var hwDesc by remember { mutableStateOf("") }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var assignedDate by remember { mutableStateOf(sdf.format(Date())) }
                var submissionDate by remember { mutableStateOf("") }

                if (submissionDate.isEmpty()) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 3)
                    submissionDate = sdf.format(cal.time)
                }

                val calendarInstance = Calendar.getInstance()

                val assignedDatePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        assignedDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                val submissionDatePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        submissionDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                var expandedDropdown by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showAddHomeworkDialog = false },
                    title = { Text("Assign New Homework", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        if (activeStudents.isEmpty()) {
                            Text("Please add active students before assigning homework.", color = ErrorRed)
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column {
                                    Text("Select Student:", style = MaterialTheme.typography.labelSmall, color = Gray)
                                    Box {
                                        Button(
                                            onClick = { expandedDropdown = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = LightGray.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(activeStudents[selectedStudentIndex].fullName, color = DarkGray)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.ArrowDropDown, "Select", tint = DarkGray)
                                        }

                                        DropdownMenu(
                                            expanded = expandedDropdown,
                                            onDismissRequest = { expandedDropdown = false }
                                        ) {
                                            activeStudents.forEachIndexed { i, student ->
                                                DropdownMenuItem(
                                                    text = { Text(student.fullName) },
                                                    onClick = {
                                                        selectedStudentIndex = i
                                                        expandedDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Assigned Date: $assignedDate", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text("Submission Date: $submissionDate", style = MaterialTheme.typography.bodySmall, color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { assignedDatePicker.show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Assigned Date", color = EmeraldGreen, fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { submissionDatePicker.show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Submission Date", color = EmeraldGreen, fontSize = 10.sp)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = hwTitle,
                                    onValueChange = { hwTitle = it },
                                    label = { Text("Homework Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )

                                OutlinedTextField(
                                    value = hwDesc,
                                    onValueChange = { hwDesc = it },
                                    label = { Text("Homework Description") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (activeStudents.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val student = activeStudents[selectedStudentIndex]
                                    viewModel.addHomework(
                                        studentId = student.studentId,
                                        title = hwTitle,
                                        description = hwDesc,
                                        assignedDate = assignedDate,
                                        submissionDate = submissionDate,
                                        onComplete = {
                                            Toast.makeText(context, "Homework assigned successfully.", Toast.LENGTH_SHORT).show()
                                            showAddHomeworkDialog = false
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("Assign", color = Color.White)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddHomeworkDialog = false }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // EDIT HOMEWORK DIALOG
            if (showEditHomeworkDialog != null) {
                val hw = showEditHomeworkDialog!!
                val activeStudents = students
                val student = activeStudents.find { it.studentId == hw.studentId }
                var hwTitle by remember { mutableStateOf(hw.title) }
                var hwDesc by remember { mutableStateOf(hw.description) }
                var hwStatus by remember { mutableStateOf(hw.status) }
                var hwFeedback by remember { mutableStateOf(hw.teacherFeedback) }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var assignedDate by remember { mutableStateOf(hw.assignedDate) }
                var submissionDate by remember { mutableStateOf(hw.submissionDate) }

                val calendarInstance = Calendar.getInstance()

                val assignedDatePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        assignedDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                val submissionDatePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        submissionDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                var statusDropdownExpanded by remember { mutableStateOf(false) }
                val statusOptions = listOf("Pending", "Submitted", "Completed")

                AlertDialog(
                    onDismissRequest = { showEditHomeworkDialog = null },
                    title = { Text("Edit Homework Assignment", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Assigned to: ${student?.fullName ?: "Unknown Student"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Assigned Date: $assignedDate", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("Submission Date: $submissionDate", style = MaterialTheme.typography.bodySmall, color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { assignedDatePicker.show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Assigned Date", color = EmeraldGreen, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { submissionDatePicker.show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Submission Date", color = EmeraldGreen, fontSize = 10.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = hwTitle,
                                onValueChange = { hwTitle = it },
                                label = { Text("Homework Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            OutlinedTextField(
                                value = hwDesc,
                                onValueChange = { hwDesc = it },
                                label = { Text("Homework Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            Column {
                                Text("Status:", style = MaterialTheme.typography.labelSmall, color = Gray)
                                Box {
                                    Button(
                                        onClick = { statusDropdownExpanded = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = LightGray.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(hwStatus, color = DarkGray)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.ArrowDropDown, "Select", tint = DarkGray)
                                    }

                                    DropdownMenu(
                                        expanded = statusDropdownExpanded,
                                        onDismissRequest = { statusDropdownExpanded = false }
                                    ) {
                                        statusOptions.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = {
                                                    hwStatus = opt
                                                    statusDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = hwFeedback,
                                onValueChange = { hwFeedback = it },
                                label = { Text("Teacher Feedback / Remarks") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val updatedHw = hw.copy(
                                    title = hwTitle,
                                    description = hwDesc,
                                    assignedDate = assignedDate,
                                    submissionDate = submissionDate,
                                    status = hwStatus,
                                    teacherFeedback = hwFeedback
                                )
                                viewModel.updateHomeworkDetails(updatedHw, {
                                    Toast.makeText(context, "Homework updated successfully.", Toast.LENGTH_SHORT).show()
                                    showEditHomeworkDialog = null
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditHomeworkDialog = null }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // EDIT LESSON DIALOG
            if (showEditLessonDialog != null) {
                val lesson = showEditLessonDialog!!
                var lessonTitle by remember { mutableStateOf(lesson.lessonTitle) }
                var surahName by remember { mutableStateOf(lesson.surahName) }
                var ayahNumber by remember { mutableStateOf(lesson.ayahNumber) }
                var homeworkDesc by remember { mutableStateOf(lesson.homework) }
                var notes by remember { mutableStateOf(lesson.teacherNotes) }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var lessonDate by remember { mutableStateOf(lesson.lessonDate) }

                val calendarInstance = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        lessonDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                AlertDialog(
                    onDismissRequest = { showEditLessonDialog = null },
                    title = { Text("Edit Tafseer Lesson", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lesson Date: $lessonDate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Button(
                                    onClick = { datePicker.show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f))
                                ) {
                                    Text("Change Date", color = EmeraldGreen, fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = lessonTitle,
                                onValueChange = { lessonTitle = it },
                                label = { Text("Lesson Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = surahName,
                                    onValueChange = { surahName = it },
                                    label = { Text("Surah Name") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                                OutlinedTextField(
                                    value = ayahNumber,
                                    onValueChange = { ayahNumber = it },
                                    label = { Text("Ayah(s)") },
                                    modifier = Modifier.weight(0.7f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                            }

                            OutlinedTextField(
                                value = homeworkDesc,
                                onValueChange = { homeworkDesc = it },
                                label = { Text("Homework Assigned") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Teacher Reflection Notes") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val updatedLesson = lesson.copy(
                                    lessonDate = lessonDate,
                                    lessonTitle = lessonTitle,
                                    surahName = surahName,
                                    ayahNumber = ayahNumber,
                                    homework = homeworkDesc,
                                    teacherNotes = notes
                                )
                                viewModel.updateLesson(updatedLesson, {
                                    Toast.makeText(context, "Lesson updated successfully.", Toast.LENGTH_SHORT).show()
                                    showEditLessonDialog = null
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditLessonDialog = null }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // EDIT TEST DIALOG
            if (showEditTestDialog != null) {
                val test = showEditTestDialog!!
                val activeStudents = students
                val studentName = activeStudents.find { it.studentId == test.studentId }?.fullName ?: "Student"
                var testTitle by remember { mutableStateOf(test.testTitle) }
                var marksObtained by remember { mutableStateOf(test.marksObtained.toString()) }
                var totalMarks by remember { mutableStateOf(test.totalMarks.toString()) }
                var remarks by remember { mutableStateOf(test.remarks) }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var testDate by remember { mutableStateOf(test.date) }

                val calendarInstance = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, dayOfMonth)
                        testDate = sdf.format(cal.time)
                    },
                    calendarInstance.get(Calendar.YEAR),
                    calendarInstance.get(Calendar.MONTH),
                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                )

                AlertDialog(
                    onDismissRequest = { showEditTestDialog = null },
                    title = { Text("Edit Test Record", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Student Name: $studentName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Test Date: $testDate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Button(
                                    onClick = { datePicker.show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f))
                                ) {
                                    Text("Change Date", color = EmeraldGreen, fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = testTitle,
                                onValueChange = { testTitle = it },
                                label = { Text("Test Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = marksObtained,
                                    onValueChange = { marksObtained = it },
                                    label = { Text("Marks Obtained") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                                OutlinedTextField(
                                    value = totalMarks,
                                    onValueChange = { totalMarks = it },
                                    label = { Text("Total Marks") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                            }

                            OutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                label = { Text("Teacher Remarks / Feedback") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val obtained = marksObtained.toDoubleOrNull() ?: -1.0
                                val total = totalMarks.toDoubleOrNull() ?: -1.0
                                val updatedTest = test.copy(
                                    testTitle = testTitle,
                                    date = testDate,
                                    marksObtained = obtained,
                                    totalMarks = total,
                                    remarks = remarks
                                )
                                viewModel.updateTest(updatedTest, {
                                    Toast.makeText(context, "Test record updated.", Toast.LENGTH_SHORT).show()
                                    showEditTestDialog = null
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditTestDialog = null }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "No data",
            tint = Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun LessonHistoryCard(
    lesson: Lesson,
    canManage: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(lesson.lessonDate, style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold)
                    Text(lesson.lessonTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkEmerald)
                }

                if (canManage) {
                    Row {
                        IconButton(onClick = { onEdit() }) {
                            Icon(Icons.Default.Edit, "Edit", tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onDelete() }) {
                            Icon(Icons.Default.Delete, "Delete", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Surah: ${lesson.surahName}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Ayah: ${lesson.ayahNumber}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (lesson.homework.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Assigned Homework:", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold)
                Text(lesson.homework, style = MaterialTheme.typography.bodySmall, color = DarkGray)
            }

            if (lesson.teacherNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Teacher Reflections:", style = MaterialTheme.typography.labelSmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                Text(lesson.teacherNotes, style = MaterialTheme.typography.bodySmall, color = Gray)
            }
        }
    }
}

@Composable
fun HomeworkCard(
    homework: Homework,
    studentName: String,
    isStudentRole: Boolean,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Student: $studentName", style = MaterialTheme.typography.labelSmall, color = Gray)
                    Text(homework.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isStudentRole) {
                        IconButton(onClick = { onEdit() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onDelete() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, "Delete", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }

                    val statusColor = when (homework.status) {
                        "Completed" -> SuccessGreen
                        "Submitted" -> Gold
                        else -> ErrorRed
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = homework.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(homework.description, style = MaterialTheme.typography.bodySmall, color = Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Assigned: ${homework.assignedDate}", style = MaterialTheme.typography.labelSmall, color = Gray)
                Text("Due: ${homework.submissionDate}", style = MaterialTheme.typography.labelSmall, color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }

            if (homework.teacherFeedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                        Text("Teacher Feedback:", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold)
                        Text(homework.teacherFeedback, style = MaterialTheme.typography.bodySmall, color = DarkEmerald)
                    }
                }
            }

            // Interactive submissions buttons
            if (isStudentRole && homework.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onStatusChange("Submitted") },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, "Submit", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Homework", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            } else if (!isStudentRole && homework.status == "Submitted") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onStatusChange("Completed") },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RateReview, "Review", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Grade & Review Submission", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ImportantNoteCard(
    note: ImportantNote,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Gold))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, "Announcements", tint = Gold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkEmerald)
                }

                if (canDelete) {
                    IconButton(onClick = { onDelete() }) {
                        Icon(Icons.Default.Delete, "Delete", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(note.note, style = MaterialTheme.typography.bodyMedium, color = DarkGray, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Published by: ${note.createdBy}", style = MaterialTheme.typography.labelSmall, color = Gray)
                Text(note.createdDate, style = MaterialTheme.typography.labelSmall, color = Gray)
            }
        }
    }
}

@Composable
fun TestCard(
    test: Test,
    studentName: String,
    canManage: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Graded Test Score", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold)
                    Text(test.testTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                }

                if (canManage) {
                    Row {
                        IconButton(onClick = { onEdit() }) {
                            Icon(Icons.Default.Edit, "Edit", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onDelete() }) {
                            Icon(Icons.Default.Delete, "Delete", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Student Name:", style = MaterialTheme.typography.labelSmall, color = Gray)
                    Text(studentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkGray)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Score Ratio:", style = MaterialTheme.typography.labelSmall, color = Gray)
                    Text("${test.percentage.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Score details: ${test.marksObtained} / ${test.totalMarks} marks scored on ${test.date}", style = MaterialTheme.typography.bodySmall, color = Gray)

            if (test.remarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                        Text("Teacher Review Remarks:", style = MaterialTheme.typography.labelSmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Text(test.remarks, style = MaterialTheme.typography.bodySmall, color = DarkEmerald)
                    }
                }
            }
        }
    }
}
