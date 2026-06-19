package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HealingViewModel
import com.example.ui.viewmodel.HealingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Singletons init
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.healingDao())
        val viewModel: HealingViewModel = ViewModelProvider(
            this,
            HealingViewModelFactory(repository)
        )[HealingViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppController(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppController(viewModel: HealingViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    
    // Bottom Tab routes:
    // Tab 0 = Home (Dashboard)
    // Tab 1 = Mood
    // Tab 2 = Distance (Temptation Blocker)
    // Tab 3 = Reflect (Journal)
    // Tab 4 = Zen (Breathe)
    // Tab 5 = Goals (Habits)
    var currentTabIndex by remember { mutableStateOf(0) }
    var showSettingsState by remember { mutableStateOf(false) }

    val validatedProfile = userProfile

    if (validatedProfile == null) {
        // Render a simple beautiful loading indicator until Room flow emits
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (!validatedProfile.onboarded) {
        // Launch dynamic 5-step Onboarding
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = { currentTabIndex = 0 }
        )
    } else if (showSettingsState) {
        // Configurations screen overlay layout
        SettingsScreen(
            viewModel = viewModel,
            onBack = { showSettingsState = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTabIndex == 0,
                        onClick = { currentTabIndex = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_home")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 1,
                        onClick = { currentTabIndex = 1 },
                        icon = { Icon(Icons.Default.Face, contentDescription = "Mood") },
                        label = { Text("Mood", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_mood")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 2,
                        onClick = { currentTabIndex = 2 },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Distance") },
                        label = { Text("Distance", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_distance")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 3,
                        onClick = { currentTabIndex = 3 },
                        icon = { Icon(Icons.Default.Create, contentDescription = "Reflect") },
                        label = { Text("Reflect", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_reflect")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 4,
                        onClick = { currentTabIndex = 4 },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Breathe") },
                        label = { Text("Zen", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_breathe")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 5,
                        onClick = { currentTabIndex = 5 },
                        icon = { Icon(Icons.Default.List, contentDescription = "Goals") },
                        label = { Text("Goals", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_goals")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 6,
                        onClick = { currentTabIndex = 6 },
                        icon = { Icon(Icons.Default.Spa, contentDescription = "Games") },
                        label = { Text("Games", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_games")
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTabIndex) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { tabIdx -> currentTabIndex = tabIdx },
                        onNavigateToSettings = { showSettingsState = true }
                    )
                    1 -> MoodScreen(viewModel = viewModel)
                    2 -> ContactDistanceScreen(viewModel = viewModel)
                    3 -> JournalScreen(viewModel = viewModel)
                    4 -> BreatheScreen(viewModel = viewModel)
                    5 -> GoalsScreen(viewModel = viewModel)
                    6 -> RelaxGamesScreen(viewModel = viewModel)
                }
            }
        }
    }
}
