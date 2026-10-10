package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.tabs.LogMeasureTab
import com.example.ui.tabs.NotifyTab
import com.example.ui.tabs.RecordsTrendsTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppThemeMode
import com.example.ui.viewmodel.BpViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BpViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = themeMode == AppThemeMode.DARK
            var showStorageInfoDialog by remember { mutableStateOf(false) }

            // Dynamically synchronize system status bar and navigation bar icon colors with Dark/Light theme
            val view = LocalView.current
            DisposableEffect(isDark) {
                val activity = view.context as? Activity
                if (activity != null) {
                    val statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                    val navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }

                    (activity as? ComponentActivity)?.enableEdgeToEdge(
                        statusBarStyle = statusBarStyle,
                        navigationBarStyle = navigationBarStyle
                    )

                    // In Dark Mode: status bar icons/text must be WHITE (isAppearanceLightStatusBars = false)
                    // In Light Mode: status bar icons/text must be DARK (isAppearanceLightStatusBars = true)
                    val insetsController = WindowCompat.getInsetsController(activity.window, view)
                    insetsController.isAppearanceLightStatusBars = !isDark
                    insetsController.isAppearanceLightNavigationBars = !isDark
                }
                onDispose { }
            }

            MyApplicationTheme(darkTheme = isDark) {
                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val lastSavedReading by viewModel.lastSavedReading.collectAsStateWithLifecycle()
                val filteredReadings by viewModel.filteredReadings.collectAsStateWithLifecycle()
                val allReadingsDesc by viewModel.allReadingsDesc.collectAsStateWithLifecycle()
                val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
                val isProUnlocked by viewModel.isProUnlocked.collectAsStateWithLifecycle()
                val reminders by viewModel.reminders.collectAsStateWithLifecycle()
                val formattedProPrice by viewModel.formattedProPrice.collectAsStateWithLifecycle()
                val billingStatusMessage by viewModel.billingStatusMessage.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(billingStatusMessage) {
                    billingStatusMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearBillingStatusMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "BP Companion",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            },
                            actions = {
                                // Storage / Privacy Info Dialog Button
                                IconButton(
                                    onClick = { showStorageInfoDialog = true },
                                    modifier = Modifier.testTag("storage_info_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Data Storage Info",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Explicit Light / Dark Mode Toggle
                                IconButton(
                                    onClick = { viewModel.toggleDarkMode() },
                                    modifier = Modifier.testTag("dark_mode_toggle_button")
                                ) {
                                    if (isDark) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = "Switch to Light Mode",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = "Switch to Dark Mode",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { viewModel.setSelectedTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Measure & Log"
                                    )
                                },
                                label = { Text("Measure & Log") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_measure_log")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { viewModel.setSelectedTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = "Records & Stats"
                                    )
                                },
                                label = { Text("Records & Stats") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_records_stats")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { viewModel.setSelectedTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = if (isProUnlocked) Icons.Default.NotificationsActive else Icons.Default.Alarm,
                                        contentDescription = "Notify"
                                    )
                                },
                                label = { Text("Notify") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_notify")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                LogMeasureTab(
                                    onSaveReading = { reading ->
                                        viewModel.saveReading(reading)
                                    },
                                    onNavigateToTrends = {
                                        viewModel.setSelectedTab(1)
                                    },
                                    lastSavedReading = lastSavedReading
                                )
                            }
                            1 -> {
                                RecordsTrendsTab(
                                    readingsForChart = filteredReadings,
                                    allReadingsDesc = allReadingsDesc,
                                    selectedPeriod = selectedPeriod,
                                    onPeriodSelected = { period ->
                                        viewModel.setPeriodFilter(period)
                                    },
                                    onDeleteReading = { reading ->
                                        viewModel.deleteReading(reading)
                                    },
                                    onAddSampleData = {
                                        viewModel.addSampleWeekData()
                                    },
                                    onNavigateToMeasure = {
                                        viewModel.setSelectedTab(0)
                                    }
                                )
                            }
                            2 -> {
                                NotifyTab(
                                    isProUnlocked = isProUnlocked,
                                    reminders = reminders,
                                    formattedPrice = formattedProPrice,
                                    onUnlockPro = { viewModel.launchGooglePlayPurchase(this@MainActivity) },
                                    onRestorePurchases = { viewModel.restoreGooglePlayPurchases() },
                                    onAddReminder = { reminder -> viewModel.addReminder(reminder) },
                                    onToggleReminder = { id, enabled -> viewModel.toggleReminder(id, enabled) },
                                    onDeleteReminder = { id -> viewModel.deleteReminder(id) }
                                )
                            }
                        }
                    }
                }

                if (showStorageInfoDialog) {
                    AlertDialog(
                        onDismissRequest = { showStorageInfoDialog = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title = {
                            Text(text = "Data Storage & Privacy")
                        },
                        text = {
                            Column {
                                Text(
                                    text = "Where are your BP readings stored?",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "All your blood pressure readings are stored 100% locally on your device in an encrypted/isolated SQLite database (via Android Room: 'calmbp_database').",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Key Benefits:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Complete Privacy: No health data is uploaded to any external server or cloud.\n• Offline Access: Always works without an internet connection.\n• Full Control: You can add or delete readings at any time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showStorageInfoDialog = false }) {
                                Text("Got it")
                            }
                        }
                    )
                }
            }
        }
    }
}
