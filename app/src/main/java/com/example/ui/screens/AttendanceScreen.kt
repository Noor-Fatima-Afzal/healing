package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.ExportUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: MainViewModel,
    currentUser: User
) {
    val context = LocalContext.current
    val students by viewModel.allStudents.collectAsState()
    val attendanceRecords by viewModel.allAttendance.collectAsState()

    var selectedDate by remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        mutableStateOf(sdf.format(Date()))
    }

    var showHistoryTab by remember { mutableStateOf(false) }

    var filterMonth by remember { mutableStateOf("All") }
    var filterYear by remember { mutableStateOf("All") }
    var searchDateQuery by remember { mutableStateOf("") }

    // Map to hold temporary attendance inputs: StudentId -> (Status, Notes)
    val attendanceMap = remember { mutableStateMapOf<Int, Pair<String, String>>() }

    // Populate the temporary map when students or date change
    LaunchedEffect(students, selectedDate) {
        attendanceMap.clear()
        // Check if there are already recorded attendances for this date in Room database
        val savedForDate = attendanceRecords.filter { it.date == selectedDate }
        students.filter { it.status == "Active" }.forEach { student ->
            val previouslySaved = savedForDate.find { it.studentId == student.studentId }
            if (previouslySaved != null) {
                attendanceMap[student.studentId] = Pair(previouslySaved.status, previouslySaved.teacherNotes)
            } else {
                // Default to Present
                attendanceMap[student.studentId] = Pair("Present", "")
            }
        }
    }

    val isStudentRole = currentUser.role == "Student"

    // Launcher for creating CSV document
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val recordsToExport = if (isStudentRole) {
                        attendanceRecords.filter { it.studentName.contains(currentUser.name, ignoreCase = true) }
                    } else {
                        attendanceRecords
                    }
                    val csvData = ExportUtils.generateCsv(recordsToExport)
                    outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                    outputStream.close()
                    Toast.makeText(context, "CSV exported successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error exporting CSV: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher for creating PDF document
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val recordsToExport = if (isStudentRole) {
                        attendanceRecords.filter { it.studentName.contains(currentUser.name, ignoreCase = true) }
                    } else {
                        attendanceRecords
                    }
                    ExportUtils.generatePdf(context, recordsToExport, outputStream)
                    outputStream.close()
                    Toast.makeText(context, "PDF exported successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error exporting PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Set up Date Picker dialog helper
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = sdf.format(cal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = WarmWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header Tabs (Record vs History)
            if (!isStudentRole) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showHistoryTab = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showHistoryTab) EmeraldGreen else Color.White,
                            contentColor = if (!showHistoryTab) Color.White else EmeraldGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Record Attendance", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showHistoryTab = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showHistoryTab) EmeraldGreen else Color.White,
                            contentColor = if (showHistoryTab) Color.White else EmeraldGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Class History", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                showHistoryTab = true // Students can only view history
            }

            // Date Selection Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Attendance Date:", style = MaterialTheme.typography.bodySmall, color = Gray)
                        Text(selectedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkEmerald)
                    }

                    if (!isStudentRole && !showHistoryTab) {
                        Button(
                            onClick = { datePickerDialog.show() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, "Pick Date", tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Date", style = MaterialTheme.typography.labelMedium, color = EmeraldGreen)
                        }
                    }
                }
            }

            // Tabs Content
            if (showHistoryTab) {
                // ATTENDANCE HISTORY LIST
                val baseHistory = if (isStudentRole) {
                    attendanceRecords.filter { it.studentName.contains(currentUser.name, ignoreCase = true) }
                } else {
                    attendanceRecords
                }

                // Apply year filter
                var filteredHistory = baseHistory
                if (filterYear != "All") {
                    filteredHistory = filteredHistory.filter { it.date.startsWith(filterYear) }
                }

                // Apply month filter
                if (filterMonth != "All") {
                    val monthNum = when (filterMonth) {
                        "Jan" -> "01"
                        "Feb" -> "02"
                        "Mar" -> "03"
                        "Apr" -> "04"
                        "May" -> "05"
                        "Jun" -> "06"
                        "Jul" -> "07"
                        "Aug" -> "08"
                        "Sep" -> "09"
                        "Oct" -> "10"
                        "Nov" -> "11"
                        "Dec" -> "12"
                        else -> ""
                    }
                    if (monthNum.isNotEmpty()) {
                        filteredHistory = filteredHistory.filter { 
                            val monthPart = it.date.split("-").getOrNull(1)
                            monthPart == monthNum
                        }
                    }
                }

                // Apply search query filter (Search by Date)
                if (searchDateQuery.trim().isNotEmpty()) {
                    filteredHistory = filteredHistory.filter { it.date.contains(searchDateQuery.trim()) }
                }

                // Always display Filter Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILTER HISTORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                            if (filterMonth != "All" || filterYear != "All" || searchDateQuery.isNotEmpty()) {
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ErrorRed,
                                    modifier = Modifier.clickable {
                                        filterMonth = "All"
                                        filterYear = "All"
                                        searchDateQuery = ""
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search Date Input
                        OutlinedTextField(
                            value = searchDateQuery,
                            onValueChange = { searchDateQuery = it },
                            label = { Text("Search Date (e.g. 2026-07)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                focusedLabelColor = EmeraldGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Month Filter Row
                        Text("Month:", style = MaterialTheme.typography.bodySmall, color = Gray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val months = listOf("All", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            months.forEach { m ->
                                val isSelected = filterMonth == m
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { filterMonth = m },
                                    label = { Text(m) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f),
                                        selectedLabelColor = EmeraldGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Year Filter Row
                        Text("Year:", style = MaterialTheme.typography.bodySmall, color = Gray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val years = listOf("All", "2026", "2025", "2024")
                            years.forEach { y ->
                                val isSelected = filterYear == y
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { filterYear = y },
                                    label = { Text(y) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f),
                                        selectedLabelColor = EmeraldGreen
                                    )
                                )
                            }
                        }
                    }
                }

                if (filteredHistory.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No history",
                            tint = Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No matching attendance records found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray
                        )
                    }
                } else {
                    // Export records card for Teachers and Helpers
                    if (!isStudentRole) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = "Export icon",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Export Records",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val fileName = "attendance_records_${sdf.format(Date())}.csv"
                                            csvLauncher.launch(fileName)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Text("Export CSV", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val fileName = "attendance_report_${sdf.format(Date())}.pdf"
                                            pdfLauncher.launch(fileName)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkEmerald),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Text("Export PDF", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Group attendance records by Date
                        val grouped = filteredHistory.groupBy { it.date }
                        grouped.forEach { (date, records) ->
                            item {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(records) { record ->
                                HistoryRecordRowItem(
                                    record = record,
                                    canDelete = !isStudentRole,
                                    onDelete = {
                                        viewModel.deleteAttendanceRecord(record) {
                                            Toast.makeText(context, "Attendance record deleted.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // RECORD ATTENDANCE INTERFACE
                val activeStudents = students.filter { it.status == "Active" }

                if (activeStudents.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = "No active students",
                            tint = Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active students found to mark attendance.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray
                        )
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(activeStudents) { student ->
                                val selectedPair = attendanceMap[student.studentId] ?: Pair("Present", "")
                                AttendanceMarkRowItem(
                                    student = student,
                                    selectedStatus = selectedPair.first,
                                    teacherNotes = selectedPair.second,
                                    onStatusChange = { newStatus ->
                                        attendanceMap[student.studentId] = Pair(newStatus, selectedPair.second)
                                    },
                                    onNotesChange = { newNotes ->
                                        attendanceMap[student.studentId] = Pair(selectedPair.first, newNotes)
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }

                        // Save Button
                        Button(
                            onClick = {
                                viewModel.recordAttendance(selectedDate, attendanceMap.toMap(), {
                                    Toast.makeText(context, "Attendance saved successfully for $selectedDate.", Toast.LENGTH_SHORT).show()
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Check, "Save", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Today's Attendance", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceMarkRowItem(
    student: com.example.data.model.Student,
    selectedStatus: String,
    teacherNotes: String,
    onStatusChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    var showNotesField by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = student.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showNotesField = !showNotesField }) {
                    Icon(
                        imageVector = if (teacherNotes.isNotEmpty()) Icons.Default.Comment else Icons.Default.AddComment,
                        contentDescription = "Add Notes",
                        tint = if (teacherNotes.isNotEmpty()) Gold else Gray.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-choice status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf("Present", "Absent", "Leave")
                options.forEach { option ->
                    val isSelected = selectedStatus == option
                    val statusColor = when (option) {
                        "Present" -> SuccessGreen
                        "Absent" -> ErrorRed
                        else -> WarningAmber
                    }

                    Button(
                        onClick = { onStatusChange(option) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) statusColor else LightGray.copy(alpha = 0.4f),
                            contentColor = if (isSelected) Color.White else DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(option, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showNotesField || teacherNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = teacherNotes,
                    onValueChange = { onNotesChange(it) },
                    placeholder = { Text("Teacher remarks / reason for absence...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun HistoryRecordRowItem(
    record: Attendance,
    canDelete: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when (record.status) {
                "Present" -> SuccessGreen
                "Absent" -> ErrorRed
                else -> WarningAmber
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(record.studentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                if (record.teacherNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(record.teacherNotes, style = MaterialTheme.typography.bodySmall, color = Gray)
                }
                
                // Show day, time, and recordedBy metadata
                val metaList = mutableListOf<String>()
                if (record.day.isNotEmpty()) metaList.add(record.day)
                if (record.time.isNotEmpty()) metaList.add(record.time)
                if (record.recordedBy.isNotEmpty()) metaList.add("By: ${record.recordedBy}")
                
                if (metaList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metaList.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray.copy(alpha = 0.8f)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = record.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (canDelete) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = ErrorRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
