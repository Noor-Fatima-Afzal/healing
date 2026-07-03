package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        // Splash Screen Route
        composable("splash") {
            SplashScreen(
                isOnboardingCompleted = isOnboardingCompleted,
                isLoggedIn = currentUser != null,
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen Route
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    viewModel.completeOnboarding()
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // Login Screen Route
        composable("login") {
            val students by viewModel.allStudents.collectAsState()
            LoginScreen(
                students = students,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginClick = { email, password, remember, onSuccess, onError ->
                    viewModel.login(email, password, remember, onSuccess, onError)
                },
                onEnrollClick = { name, email, role, onSuccess, onError ->
                    viewModel.enrollUser(name, email, role, onSuccess, onError)
                }
            )
        }

        // Main Dashboard Screens Container with Bottom Navigation Row
        composable("main") {
            val user = currentUser
            if (user != null) {
                MainContainer(
                    viewModel = viewModel,
                    currentUser = user,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            } else {
                // Fail-safe fall back to login if session lost
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContainer(
    viewModel: MainViewModel,
    currentUser: com.example.data.model.User,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabLabels = if (currentUser.role == "Student") {
        listOf("Home", "My Progress", "Attendance", "Lessons", "Settings")
    } else {
        listOf("Home", "Students", "Attendance", "Lessons", "Profile")
    }
    val activeIcons = listOf(Icons.Filled.Home, Icons.Filled.Person, Icons.Filled.CalendarMonth, Icons.Filled.MenuBook, Icons.Filled.Person)
    val inactiveIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Person, Icons.Outlined.CalendarMonth, Icons.Outlined.MenuBook, Icons.Outlined.Person)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) activeIcons[index] else inactiveIcons[index],
                                contentDescription = label
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldGreen,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    onNavigateToStudents = { selectedTab = 1 },
                    onNavigateToAttendance = { selectedTab = 2 },
                    onNavigateToLessons = { selectedTab = 3 },
                    onNavigateToProfile = { selectedTab = 4 }
                )
                1 -> StudentsScreen(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                2 -> AttendanceScreen(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                3 -> LessonsScreen(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                4 -> ProfileScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    onLogout = onLogout
                )
            }
        }
    }
}
