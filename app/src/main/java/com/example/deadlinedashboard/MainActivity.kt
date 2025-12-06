package com.example.deadlinedashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.deadlinedashboard.data.ThemeRepository
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.ui.add_assignment.AddAssignmentSheet
import com.example.deadlinedashboard.ui.dashboard.DashboardScreen
import com.example.deadlinedashboard.ui.dashboard.SortOption
import com.example.deadlinedashboard.ui.settings.SettingsScreen
import com.example.deadlinedashboard.ui.summary.SummaryScreen
import com.example.deadlinedashboard.ui.theme.DeadlineDashboardTheme
import com.example.deadlinedashboard.ui.weekly.WeeklyOverviewScreen
import com.example.deadlinedashboard.viewmodel.AssignmentViewModel
import com.example.deadlinedashboard.viewmodel.ThemeViewModel
import com.example.deadlinedashboard.viewmodel.ThemeViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: AssignmentViewModel by viewModels()
    private val TAG = "MainActivity"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.d(TAG, "Notification permission denied")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        askNotificationPermission()

        setContent {
            val themeRepository = remember { ThemeRepository(applicationContext) }
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(themeRepository))

            DeadlineDashboardTheme(themeViewModel = themeViewModel) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                var showBottomSheet by remember { mutableStateOf(false) }
                var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
                var sortOption by remember { mutableStateOf(SortOption.Urgency) }
                var showSortMenu by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Assignment Deadline Dashboard") },
                            navigationIcon = {
                                if (currentDestination?.route != "dashboard") {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { 
                                    Log.d(TAG, "More options menu clicked")
                                    showSortMenu = true 
                                }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(text = { Text("Home") }, onClick = { 
                                        Log.d(TAG, "Home clicked")
                                        navController.navigate("dashboard"); showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Sort by Urgency") }, onClick = { 
                                        Log.d(TAG, "Sort by Urgency clicked")
                                        sortOption = SortOption.Urgency; showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Sort by Deadline") }, onClick = { 
                                        Log.d(TAG, "Sort by Deadline clicked")
                                        sortOption = SortOption.Deadline; showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Sort by Subject") }, onClick = { 
                                        Log.d(TAG, "Sort by Subject clicked")
                                        sortOption = SortOption.Subject; showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Sort by Weightage") }, onClick = { 
                                        Log.d(TAG, "Sort by Weightage clicked")
                                        sortOption = SortOption.Weightage; showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Weekly Overview") }, onClick = { 
                                        Log.d(TAG, "Weekly Overview clicked")
                                        navController.navigate("weekly"); showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Summary") }, onClick = { 
                                        Log.d(TAG, "Summary clicked")
                                        navController.navigate("summary"); showSortMenu = false 
                                    })
                                    DropdownMenuItem(text = { Text("Settings") }, onClick = { 
                                        Log.d(TAG, "Settings clicked")
                                        navController.navigate("settings"); showSortMenu = false 
                                    })
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { 
                            Log.d(TAG, "FAB clicked")
                            editingAssignment = null
                            showBottomSheet = true 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Assignment")
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController, 
                        startDestination = "dashboard",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("dashboard") {
                            Log.d(TAG, "Navigating to dashboard")
                            DashboardScreen(
                                viewModel = viewModel,
                                sortOption = sortOption,
                                onEditClick = {
                                    Log.d(TAG, "Editing assignment: $it")
                                    editingAssignment = it
                                    showBottomSheet = true
                                }
                            )
                        }
                        composable("weekly") {
                            Log.d(TAG, "Navigating to weekly overview")
                            WeeklyOverviewScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
                        }
                        composable("settings") {
                            Log.d(TAG, "Navigating to settings")
                            SettingsScreen(themeViewModel = themeViewModel)
                        }
                        composable("summary") {
                            Log.d(TAG, "Navigating to summary")
                            SummaryScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
                        }
                    }

                    if (showBottomSheet) {
                        Log.d(TAG, "Showing bottom sheet")
                        AddAssignmentSheet(
                            viewModel = viewModel,
                            assignment = editingAssignment,
                            onDismiss = { 
                                Log.d(TAG, "Dismissing bottom sheet")
                                showBottomSheet = false 
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}