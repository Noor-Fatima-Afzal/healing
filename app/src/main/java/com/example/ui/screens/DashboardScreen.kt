package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Calendar
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.IslamicDecorativeBorder
import com.example.ui.components.OfflineModeBanner
import com.example.ui.theme.*
import com.example.utils.DynamicIslamicContent
import com.example.utils.ContentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    currentUser: User,
    onNavigateToStudents: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToLessons: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.allStudents.collectAsState()
    val lessons by viewModel.allLessons.collectAsState()
    val homework by viewModel.allHomework.collectAsState()
    val attendanceRecords by viewModel.allAttendance.collectAsState()
    val favoriteDuas by viewModel.favoriteDuas.collectAsState()

    // Calculate Summary Stats
    val totalStudentsCount = students.size
    val activeStudentsCount = students.count { it.status == "Active" }
    val inactiveStudentsCount = students.count { it.status == "Inactive" }
    val pendingHomeworkCount = homework.count { it.status == "Pending" }
    val lessonsCompletedCount = lessons.size

    val isOnline by viewModel.isOnline.collectAsState()

    // Rotating elements
    val verse = viewModel.todayQuranVerse
    val hadith = viewModel.todayHadith
    val dua = viewModel.todayDua
    val reminder = viewModel.todayReminder

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Offline Mode Banner
        item {
            OfflineModeBanner(isOnline = isOnline)
        }

        // 1. Warm Islamic Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkEmerald
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "السلام عليكم ورحمة الله وبركاته",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Healing With Quran",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Welcome Back, ${currentUser.name}! (${currentUser.role})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PaleGold,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        val syncStatus by viewModel.syncStatus.collectAsState()
                        val dotColor = when (syncStatus) {
                            "Synced" -> EmeraldGreen
                            "Syncing" -> Gold
                            "Offline" -> Color.Gray
                            else -> Color.Red
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sync: $syncStatus",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PaleGold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Gregorian Date",
                                tint = Gold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.gregorianDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmWhite
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Hijri Date",
                                tint = Gold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.hijriDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Daily Quran Verse Card (with decoration) - Now Dynamic Random Islamic Content Card
        item {
            val randomContentState by viewModel.randomIslamicContent.collectAsState()
            val content = randomContentState ?: DynamicIslamicContent(
                type = ContentType.AYAH,
                arabicText = "وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِّلْمُؤْمِنِينَ",
                urduTranslation = "اور ہم قرآن میں سے وہ چیز نازل کرتے ہیں جو ایمان والوں کے لیے شفا اور رحمت ہے۔",
                englishTranslation = "And We send down of the Qur'an that which is healing and mercy for the believers.",
                surahName = "Al-Isra",
                ayahNumber = "82"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                IslamicDecorativeBorder(modifier = Modifier.fillMaxWidth(), borderColor = Gold) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cardTitle = when (content.type) {
                                ContentType.AYAH -> "DAILY QURANIC VERSE"
                                ContentType.HADITH -> "DAILY HADITH REFLECTION"
                                ContentType.DUA -> "DAILY DUA (SUPPLICATION)"
                            }
                            Text(
                                text = cardTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = Gold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val shareText = when (content.type) {
                                            ContentType.AYAH -> "Daily Quran Verse:\n\n${content.arabicText}\n\n${content.englishTranslation}\n\n[Surah ${content.surahName}, Ayah ${content.ayahNumber}]"
                                            ContentType.HADITH -> "Daily Hadith:\n\n${content.arabicText}\n\n${content.englishTranslation}\n\n[Book: ${content.bookName}, Ref: ${content.reference}]"
                                            ContentType.DUA -> "Daily Dua:\n\n${content.arabicText}\n\n${content.englishTranslation}\n\n[Source: ${content.source}]"
                                        }
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Gold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = content.arabicText,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkEmerald,
                                fontSize = 22.sp,
                                lineHeight = 34.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = content.urduTranslation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = EmeraldGreen,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = content.englishTranslation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DarkGray,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val metaText = when (content.type) {
                            ContentType.AYAH -> "Surah ${content.surahName} — Ayah ${content.ayahNumber}"
                            ContentType.HADITH -> "Book: ${content.bookName} — Ref: ${content.reference}"
                            ContentType.DUA -> "Source: ${content.source}"
                        }

                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }

        // 3. Class Schedule & WhatsApp Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.sweepGradient(listOf(Gold, PaleGold)))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call icon",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TODAY'S TAFSEER CLASS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkEmerald
                                )
                            )
                        }

                        // Status Badge (Live or Upcoming depending on time)
                        val calendar = Calendar.getInstance()
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        val totalMinutes = hour * 60 + minute
                        val startClassMin = 17 * 60 + 30 // 5:30 PM
                        val endClassMin = 18 * 60 + 30   // 6:30 PM

                        val (statusText, statusBg, statusFg) = when {
                            totalMinutes in startClassMin..endClassMin -> Triple("LIVE NOW", SuccessGreen, Color.White)
                            totalMinutes > endClassMin -> Triple("COMPLETED", Gray, Color.White)
                            else -> Triple("UPCOMING", Gold, DarkEmerald)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusFg
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tafseer & Refinement Class",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkEmerald
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Time:", style = MaterialTheme.typography.bodySmall, color = Gray)
                            Text("5:30 PM – 6:30 PM", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkGray)
                        }
                        Column {
                            Text("Teacher:", style = MaterialTheme.typography.bodySmall, color = Gray)
                            Text("Bint e Khalid", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkGray)
                        }
                        Column {
                            Text("Platform:", style = MaterialTheme.typography.bodySmall, color = Gray)
                            Text("WhatsApp Call", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/Csxqt53tdho2H9vOHoxCQM?s=sw&p=a&mlu=2"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp is not installed or available on this device.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "WhatsApp Group",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Healing with Quran", 
                                style = MaterialTheme.typography.labelMedium, 
                                color = Color.White,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/923100471575"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp is not installed. Attempting direct call...", Toast.LENGTH_SHORT).show()
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+923100471575"))
                                        context.startActivity(dialIntent)
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "Call features are not available.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = "Contact Helper",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Call Helper", 
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), 
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "+92 310 0471575", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Summary Section (If Teacher, Helper, or Admin)
        if (currentUser.role == "Teacher" || currentUser.role == "Helper" || currentUser.role == "Admin") {
            item {
                Text(
                    text = "ACADEMY SNAPSHOT",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatsCard(
                        title = "Total Students",
                        value = totalStudentsCount.toString(),
                        icon = Icons.Default.People,
                        color = DarkEmerald,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToStudents() }
                    )
                    StatsCard(
                        title = "Active Students",
                        value = activeStudentsCount.toString(),
                        icon = Icons.Default.Person,
                        color = SuccessGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToStudents() }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatsCard(
                        title = "Pending Homework",
                        value = pendingHomeworkCount.toString(),
                        icon = Icons.Default.Assignment,
                        color = WarningAmber,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToLessons() }
                    )
                    StatsCard(
                        title = "Lessons Completed",
                        value = lessonsCompletedCount.toString(),
                        icon = Icons.Default.MenuBook,
                        color = Gold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToLessons() }
                    )
                }
            }
        }

        // Student Personalized Dashboard Panel
        if (currentUser.role == "Student") {
            val currentStudent = students.find { it.fullName.contains(currentUser.name, ignoreCase = true) }
            val studentAttendance = attendanceRecords.filter { it.studentName.contains(currentUser.name, ignoreCase = true) }
            val studentTotalClasses = studentAttendance.size
            val studentPresentDays = studentAttendance.count { it.status == "Present" }
            val studentAbsentDays = studentAttendance.count { it.status == "Absent" }
            val studentLeaveDays = studentAttendance.count { it.status == "Leave" }
            val studentAttendancePercentage = if (studentTotalClasses > 0) {
                (studentPresentDays.toFloat() / studentTotalClasses.toFloat() * 100).toInt()
            } else {
                currentStudent?.attendancePercentage ?: 100
            }

            item {
                Text(
                    text = "MY ACADEMY OVERVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATTENDANCE SUMMARY",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Attendance",
                                tint = Gold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text(studentTotalClasses.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Present", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text("✅ $studentPresentDays", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Absent", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text("❌ $studentAbsentDays", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Leave", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text("🟡 $studentLeaveDays", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarningAmber)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Ratio", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text("$studentAttendancePercentage%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }
                    }
                }
            }

            // Tafseer Progress Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.sweepGradient(listOf(Gold, PaleGold)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MY TAFSEER PROGRESS",
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
                                Text("Current Surah:", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text(currentStudent?.currentSurah ?: "Not Started", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkEmerald)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Current Ayah:", style = MaterialTheme.typography.bodySmall, color = Gray)
                                Text(currentStudent?.currentAyah ?: "—", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkEmerald)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Indicator
                        val progressValue = if (currentStudent != null) {
                            val surahIndex = when (currentStudent.currentSurah.trim()) {
                                "Al-Baqarah" -> 2
                                "Al-Imran" -> 3
                                "An-Nisa" -> 4
                                "Al-Ma'idah" -> 5
                                else -> 1
                            }
                            (surahIndex.toFloat() / 114f).coerceIn(0.1f, 1.0f)
                        } else {
                            0.05f
                        }

                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = EmeraldGreen,
                            trackColor = PaleGold
                        )
                    }
                }
            }
        }

        // 5. Daily Hadith Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY HADITH REFLECTION",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Hadith Icon",
                            tint = Gold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = hadith.arabic,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkEmerald,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Right
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = hadith.urdu,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = EmeraldGreen,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = hadith.english,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DarkGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Source: ${hadith.reference}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray
                    )
                }
            }
        }

        // 6. Daily Dua Card
        item {
            val isFav = favoriteDuas.contains(viewModel.todayDayOfMonth)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY DUA (SUPPLICATION)",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                viewModel.toggleDuaFavorite(viewModel.todayDayOfMonth)
                                if (isFav) {
                                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Dua Favorite",
                                tint = if (isFav) ErrorRed else Gold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dua.arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkEmerald,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Urdu: ${dua.urdu}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldGreen)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "English: ${dua.english}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = DarkGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Reference: ${dua.reference}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray
                    )
                }
            }
        }

        // 7. Inspirational Spiritual Reminder
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SoftGoldBg
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "Spiritual reminder",
                            tint = EmeraldGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SPIRITUAL REMINDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$reminder\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 8. Recent Class Activity Timeline
        item {
            Text(
                text = "RECENT CLASS ACTIVITY",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
            )
        }

        if (lessons.isEmpty() && attendanceRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No recent class activity recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            // Display static chronological activity nodes
            val listSize = lessons.size.coerceAtMost(3)
            val subsetLessons = lessons.take(listSize)
            items(subsetLessons) { lesson ->
                ActivityTimelineNode(
                    title = "New Tafseer Lesson Completed",
                    subtitle = "Topic: ${lesson.lessonTitle} (Surah ${lesson.surahName})",
                    timestamp = lesson.lessonDate,
                    icon = Icons.Default.MenuBook,
                    iconTint = EmeraldGreen
                )
            }
        }

        // Space at bottom for navigation bar clearance
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                )
            }
        }
    }
}

@Composable
fun ActivityTimelineNode(
    title: String,
    subtitle: String,
    timestamp: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            // Vertical timeline line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(30.dp)
                    .background(Gray.copy(alpha = 0.2f))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
            }
        }
    }
}
