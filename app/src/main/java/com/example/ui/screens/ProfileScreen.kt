package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    currentUser: User,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val lessons by viewModel.allLessons.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = WarmWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Big Profile Avatar with Initials
            val initials = currentUser.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Name & Role
            Text(
                text = currentUser.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "${currentUser.role} Account",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkEmerald,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tafseer Academy: Healing With Quran",
                style = MaterialTheme.typography.bodySmall,
                color = Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // HELPER INFORMATION CARD (Required in Part 1)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACADEMY SUPPORT HELPER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text("Nida", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = DarkGray)
                            Text("+92 310 0471575", style = MaterialTheme.typography.bodyMedium, color = Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+923100471575"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, "Dial", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Helper", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val uri = Uri.parse("https://wa.me/923100471575")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, "WhatsApp", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SETTINGS & QUICK ACTIONS (Part 2 Settings list)
            Text(
                text = "APPLICATION SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Left
            )

            // 1. Dark Mode Settings row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, "Dark Mode", tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Dark Theme Mode", style = MaterialTheme.typography.bodyMedium, color = DarkGray)
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                    )
                }
            }

            // 2. Change Password row
            SettingsRowItem(
                title = "Change Password",
                icon = Icons.Default.Lock,
                onClick = { showChangePasswordDialog = true }
            )

            // 3. About App row
            SettingsRowItem(
                title = "About Application",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )

            // 4. Privacy Policy row
            SettingsRowItem(
                title = "Privacy Policy",
                icon = Icons.Default.PrivacyTip,
                onClick = { showPrivacyDialog = true }
            )

            // 5. Logout row
            SettingsRowItem(
                title = "Sign Out",
                icon = Icons.Default.Logout,
                color = ErrorRed,
                onClick = {
                    viewModel.logout()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(80.dp))

            // PASSWORD CHANGE DIALOG
            if (showChangePasswordDialog) {
                var oldPassword by remember { mutableStateOf("") }
                var newPassword by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showChangePasswordDialog = false },
                    title = { Text("Change Password", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Current Password") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.changePassword(oldPassword, newPassword, {
                                    Toast.makeText(context, "Password changed successfully.", Toast.LENGTH_SHORT).show()
                                    showChangePasswordDialog = false
                                }, { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Update", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChangePasswordDialog = false }) {
                            Text("Cancel", color = EmeraldGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // ABOUT APPLICATION DIALOG
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("About Healing With Quran", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Version: 1.0.0 (Production)", style = MaterialTheme.typography.bodySmall, color = Gray)
                            Text(
                                "Healing With Quran is a luxury Tafseer Class Management System designed by Academy Bint e Khalid.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGray
                            )
                            Text(
                                "Conducting classes via WhatsApp Calls, the academy nurtures Hope, Hope, Reflection, hope, hope and spiritual connection with the Holy Quran.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkEmerald
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showAboutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    },
                    containerColor = Color.White
                )
            }

            // PRIVACY POLICY DIALOG
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("Privacy Policy", color = DarkEmerald, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("Data Safeguards", style = MaterialTheme.typography.titleSmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                            Text(
                                "All student marks, oral tests, names, and attendance values are cached securely locally on-device and synchronized with Firebase Firestore using robust offline rules.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkGray
                            )
                            Text(
                                "We do not sell, leak, or share any personal files or phone directories with secondary analytical parties. Privacy is a trust from Allah.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkGray
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showPrivacyDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("I Understand", color = Color.White)
                        }
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = EmeraldGreen,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, title, tint = color)
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = if (color == ErrorRed) ErrorRed else DarkGray, fontWeight = if (color == ErrorRed) FontWeight.Bold else FontWeight.Normal)
            }

            Icon(Icons.Default.ChevronRight, "Navigate", tint = Gray.copy(alpha = 0.5f))
        }
    }
}
