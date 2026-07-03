package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: MainViewModel,
    currentUser: User
) {
    val context = LocalContext.current
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val statusFilter by viewModel.studentStatusFilter.collectAsState()
    val filteredStudents by viewModel.filteredStudents.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var showEditDialog by remember { mutableStateOf<Student?>(null) }

    // If the logged-in user is a "Student", they must only see their own profile!
    val isStudentRole = currentUser.role == "Student"

    LaunchedEffect(isStudentRole, filteredStudents) {
        if (isStudentRole && filteredStudents.isNotEmpty()) {
            // Find the student record matching their name
            val match = filteredStudents.find { it.fullName.contains(currentUser.name, ignoreCase = true) }
                ?: filteredStudents.firstOrNull()
            selectedStudentForDetail = match
        }
    }

    Scaffold(
        containerColor = WarmWhite,
        floatingActionButton = {
            if (!isStudentRole && selectedStudentForDetail == null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = EmeraldGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_student_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isStudentRole) {
                // If student role, just show their own detailed profile card directly!
                if (selectedStudentForDetail != null) {
                    StudentProfileDetails(
                        student = selectedStudentForDetail!!,
                        viewModel = viewModel,
                        canEdit = false,
                        onBack = null,
                        onEdit = {},
                        onDelete = {}
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldGreen)
                    }
                }
            } else {
                // Teacher / Helper View: List & Detail flow
                if (selectedStudentForDetail != null) {
                    // Show detailed view of selected student
                    StudentProfileDetails(
                        student = selectedStudentForDetail!!,
                        viewModel = viewModel,
                        canEdit = currentUser.role == "Teacher" || currentUser.role == "Helper" || currentUser.role == "Admin", // teacher, helper, or admin can edit / archive fully
                        onBack = { selectedStudentForDetail = null },
                        onEdit = {
                            showEditDialog = selectedStudentForDetail
                        },
                        onDelete = {
                            viewModel.deleteStudent(selectedStudentForDetail!!.studentId) {
                                Toast.makeText(context, "Student deleted successfully.", Toast.LENGTH_SHORT).show()
                                selectedStudentForDetail = null
                            }
                        }
                    )
                } else {
                    // Show List View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Search & Filter header
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setStudentSearchQuery(it) },
                            placeholder = { Text("Search by student name...") },
                            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = EmeraldGreen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_search"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Filter chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf("All", "Active", "Inactive")
                            filters.forEach { filter ->
                                val isSelected = statusFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setStudentStatusFilter(filter) },
                                    label = { Text(filter) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Students List
                        if (filteredStudents.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PeopleOutline,
                                    contentDescription = "No students",
                                    tint = Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No students found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredStudents) { student ->
                                    StudentRowItem(
                                        student = student,
                                        onClick = { selectedStudentForDetail = student }
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ADD STUDENT DIALOG
            if (showAddDialog) {
                var name by remember { mutableStateOf("") }
                var notes by remember { mutableStateOf("") }
                var status by remember { mutableStateOf("Active") }

                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Add New Student", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name (Required)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            // Status Dropdown
                            Column {
                                Text("Status:", style = MaterialTheme.typography.labelMedium, color = Gray)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    listOf("Active", "Inactive").forEach { s ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { status = s }
                                        ) {
                                            RadioButton(
                                                selected = status == s,
                                                onClick = { status = s },
                                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                            )
                                            Text(s)
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("General Notes (Optional)") },
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
                                viewModel.addStudent(name, status, notes, {
                                    Toast.makeText(context, "Student added successfully.", Toast.LENGTH_SHORT).show()
                                    showAddDialog = false
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
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // EDIT STUDENT DIALOG
            if (showEditDialog != null) {
                val student = showEditDialog!!
                var name by remember { mutableStateOf(student.fullName) }
                var status by remember { mutableStateOf(student.status) }
                var notes by remember { mutableStateOf(student.generalNotes) }
                var currentSurah by remember { mutableStateOf(student.currentSurah) }
                var currentAyah by remember { mutableStateOf(student.currentAyah) }

                AlertDialog(
                    onDismissRequest = { showEditDialog = null },
                    title = { Text("Edit Student Profile", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name (Required)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )

                            // Status Choice
                            Column {
                                Text("Status:", style = MaterialTheme.typography.labelMedium, color = Gray)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    listOf("Active", "Inactive").forEach { s ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { status = s }
                                        ) {
                                            RadioButton(
                                                selected = status == s,
                                                onClick = { status = s },
                                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                            )
                                            Text(s)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentSurah,
                                    onValueChange = { currentSurah = it },
                                    label = { Text("Current Surah") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                                OutlinedTextField(
                                    value = currentAyah,
                                    onValueChange = { currentAyah = it },
                                    label = { Text("Ayah") },
                                    modifier = Modifier.weight(0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                                )
                            }

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("General Notes") },
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
                                viewModel.updateStudent(student.studentId, name, status, notes, currentSurah, currentAyah, {
                                    Toast.makeText(context, "Student profile updated.", Toast.LENGTH_SHORT).show()
                                    // Update details screen with updated student
                                    selectedStudentForDetail = student.copy(
                                        fullName = name,
                                        status = status,
                                        generalNotes = notes,
                                        currentSurah = currentSurah,
                                        currentAyah = currentAyah
                                    )
                                    showEditDialog = null
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
                        TextButton(onClick = { showEditDialog = null }) {
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
fun StudentRowItem(
    student: Student,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Initials Avatar
            val initials = student.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge
                    val badgeColor = if (student.status == "Active") SuccessGreen else Gray
                    Card(
                        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = student.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Attendance: ${student.attendancePercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = Gold
            )
        }
    }
}

@Composable
fun StudentProfileDetails(
    student: Student,
    viewModel: MainViewModel,
    canEdit: Boolean,
    onBack: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val allHomework by viewModel.allHomework.collectAsState()
    val allTests by viewModel.allTests.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    // Filter student records
    val studentHomework = allHomework.filter { it.studentId == student.studentId }
    val studentTests = allTests.filter { it.studentId == student.studentId }
    val studentAttendance = allAttendance.filter { it.studentId == student.studentId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Back Bar / Action bar
        if (onBack != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = EmeraldGreen)
                    }

                    Text(
                        text = "Student Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkEmerald
                    )

                    if (canEdit) {
                        Row {
                            IconButton(onClick = { onEdit() }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldGreen)
                            }
                            IconButton(
                                onClick = {
                                    // Confirm Delete
                                    onDelete()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        }

        // Header Avatar and Name card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val initials = student.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val badgeColor = if (student.status == "Active") SuccessGreen else Gray
                    Card(
                        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = student.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (student.generalNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = student.generalNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Attendance stats cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Attended", style = MaterialTheme.typography.bodySmall, color = Gray)
                        Text("${student.classesAttended}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Missed", style = MaterialTheme.typography.bodySmall, color = Gray)
                        Text("${student.classesMissed}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                }

                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ratio %", style = MaterialTheme.typography.bodySmall, color = Gray)
                        Text("${student.attendancePercentage}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                }
            }
        }

        // Tafseer Journey progress
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Gold, PaleGold)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TAFSEER JOURNEY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current surah:", style = MaterialTheme.typography.bodySmall, color = Gray)
                            Text(student.currentSurah, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkEmerald)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current Ayah:", style = MaterialTheme.typography.bodySmall, color = Gray)
                            Text(student.currentAyah, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkEmerald)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated beautifully styled linear progress indicator
                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = EmeraldGreen,
                        trackColor = PaleGold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lessons", style = MaterialTheme.typography.labelSmall, color = Gray)
                            Text("${student.totalLessonsCompleted}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Homework", style = MaterialTheme.typography.labelSmall, color = Gray)
                            Text("${student.homeworkCompleted}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tests", style = MaterialTheme.typography.labelSmall, color = Gray)
                            Text("${student.testsCompleted}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                        }
                    }
                }
            }
        }

        // Historical Homework Assigned & Tests
        item {
            Text(
                text = "HOMEWORK SUMMARY",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (studentHomework.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No homework assigned to this student yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        } else {
            items(studentHomework) { hw ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(hw.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            
                            val hwColor = when (hw.status) {
                                "Completed" -> SuccessGreen
                                "Submitted" -> Gold
                                else -> ErrorRed
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = hwColor.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    hw.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = hwColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(hw.description, style = MaterialTheme.typography.bodySmall, color = Gray)

                        if (hw.teacherFeedback.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Feedback: ${hw.teacherFeedback}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Test records
        item {
            Text(
                text = "TEST RECORDS",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
        }

        if (studentTests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No tests recorded for this student yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        } else {
            items(studentTests) { test ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(test.testTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("${test.percentage.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Marks: ${test.marksObtained} / ${test.totalMarks}  •  Date: ${test.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray
                        )
                        if (test.remarks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Remarks: ${test.remarks}", style = MaterialTheme.typography.bodySmall, color = DarkGray)
                        }
                    }
                }
            }
        }

        // Spacer at bottom
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
