package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IslamicAppLogo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    students: List<com.example.data.model.Student> = emptyList(),
    onLoginSuccess: () -> Unit,
    onLoginClick: (String, String, Boolean, () -> Unit, (String) -> Unit) -> Unit,
    onEnrollClick: (String, String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showStudentPicker by remember { mutableStateOf(false) }

    var showEnrollDialog by remember { mutableStateOf(false) }
    var enrollName by remember { mutableStateOf("") }
    var enrollEmail by remember { mutableStateOf("") }
    var enrollRole by remember { mutableStateOf("Helper") }
    var enrollIsLoading by remember { mutableStateOf(false) }
    var enrollErrorText by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    if (showStudentPicker) {
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = {
                Text(
                    text = "Select Student Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkEmerald
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose any student from the database to autofill their unique credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(modifier = Modifier.heightIn(max = 280.dp)) {
                        val displayStudents = students.ifEmpty {
                            listOf(
                                com.example.data.model.Student(fullName = "Iqra Afzal"),
                                com.example.data.model.Student(fullName = "Ali Hassan"),
                                com.example.data.model.Student(fullName = "Fatima Noor"),
                                com.example.data.model.Student(fullName = "Ayesha Khan")
                            )
                        }
                        
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(displayStudents.size) { index ->
                                val studentName = displayStudents[index].fullName
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            email = studentName.lowercase().replace(" ", ".") + "@healing.com"
                                            password = "student123"
                                            errorText = null
                                            showStudentPicker = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = studentName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DarkGray
                                            )
                                            Text(
                                                text = studentName.lowercase().replace(" ", ".") + "@healing.com",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Gray
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Select",
                                            modifier = Modifier.size(16.dp),
                                            tint = EmeraldGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentPicker = false }) {
                    Text("Close", color = EmeraldGreen)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEnrollDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!enrollIsLoading) {
                    showEnrollDialog = false
                    enrollName = ""
                    enrollEmail = ""
                    enrollRole = "Helper"
                    enrollErrorText = null
                }
            },
            title = {
                Text(
                    text = "Staff Enrollment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkEmerald
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Register a new Teacher or Helper account. Enrolled staff can immediately sign in and manage attendance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )

                    if (enrollErrorText != null) {
                        Text(
                            text = enrollErrorText ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = enrollName,
                        onValueChange = { 
                            enrollName = it
                            enrollErrorText = null
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Professor Ahmed") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, "Name", tint = EmeraldGreen)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen
                        )
                    )

                    OutlinedTextField(
                        value = enrollEmail,
                        onValueChange = { 
                            enrollEmail = it
                            enrollErrorText = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. ahmed@healing.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, "Email", tint = EmeraldGreen)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen
                        )
                    )

                    Text(
                        text = "Select Role:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Helper", "Teacher").forEach { role ->
                            val isSelected = enrollRole == role
                            OutlinedCard(
                                onClick = { enrollRole = role },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.12f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) EmeraldGreen else Gray.copy(alpha = 0.5f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { enrollRole = role },
                                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                        )
                                        Text(
                                            text = role,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) DarkEmerald else DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val passwordNote = if (enrollRole == "Teacher") "teacher123" else "helper123"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Note: Your account password will be set to: $passwordNote. You can change this later in your profile settings.",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkEmerald,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        enrollIsLoading = true
                        enrollErrorText = null
                        onEnrollClick(enrollName, enrollEmail, enrollRole, {
                            enrollIsLoading = false
                            showEnrollDialog = false
                            email = enrollEmail
                            password = if (enrollRole == "Teacher") "teacher123" else "helper123"
                            onLoginSuccess()
                        }, { err ->
                            enrollIsLoading = false
                            enrollErrorText = err
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    enabled = !enrollIsLoading && enrollName.isNotBlank() && enrollEmail.isNotBlank()
                ) {
                    if (enrollIsLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Enroll Staff")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEnrollDialog = false
                        enrollName = ""
                        enrollEmail = ""
                        enrollRole = "Helper"
                        enrollErrorText = null
                    },
                    enabled = !enrollIsLoading
                ) {
                    Text("Cancel", color = Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = WarmWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Decorative arch or spacing
                Spacer(modifier = Modifier.height(16.dp))

                // App Logo
                IslamicAppLogo()

                Spacer(modifier = Modifier.height(16.dp))

                // Heading
                Text(
                    text = "Healing With Quran",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = DarkEmerald,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Healing Hearts Through the Light of Quran",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Gold,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Error Message Card
                if (errorText != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorText ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorText = null
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("email@healing.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = EmeraldGreen
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = Gray.copy(alpha = 0.5f),
                        focusedLabelColor = EmeraldGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorText = null
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = EmeraldGreen
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                tint = EmeraldGreen
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = Gray.copy(alpha = 0.5f),
                        focusedLabelColor = EmeraldGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Remember Me and Forgot Password row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                        )
                        Text(
                            text = "Remember Me",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGray
                        )
                    }

                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable {
                            errorText = "Password recovery link has been simulated. Please use the fallback test accounts below to sign in."
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Login Button
                Button(
                    onClick = {
                        isLoading = true
                        errorText = null
                        onLoginClick(email, password, rememberMe, {
                            isLoading = false
                            onLoginSuccess()
                        }, { err ->
                            isLoading = false
                            errorText = err
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Premium Demo Quick-Login section (extremely useful for previewers!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SoftGoldBg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Gold, PaleGold)))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Quick Tap to Autofill Credentials:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkEmerald
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Teacher Account Autofill
                        Button(
                            onClick = {
                                email = "teacher@healing.com"
                                password = "teacher123"
                                errorText = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Teacher: Bint e Khalid", style = MaterialTheme.typography.labelSmall, color = DarkEmerald)
                                Icon(Icons.Default.ArrowForward, "Autofill", modifier = Modifier.size(12.dp), tint = EmeraldGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Helper Account Autofill
                        Button(
                            onClick = {
                                email = "helper@healing.com"
                                password = "helper123"
                                errorText = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Helper: Nida", style = MaterialTheme.typography.labelSmall, color = DarkEmerald)
                                Icon(Icons.Default.ArrowForward, "Autofill", modifier = Modifier.size(12.dp), tint = EmeraldGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Student Account Autofill Selector
                        Button(
                            onClick = {
                                showStudentPicker = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Student: Tap to Choose Student...", style = MaterialTheme.typography.labelSmall, color = DarkEmerald)
                                Icon(Icons.Default.ArrowDropDown, "Select", modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
