package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DashboardScreen
import com.example.ui.DashboardViewModel
import com.example.ui.screens.*
import com.example.ui.theme.AutomotiveBlack
import com.example.ui.theme.AutomotiveSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
                val tripA by viewModel.tripA.collectAsStateWithLifecycle()
                val tripB by viewModel.tripB.collectAsStateWithLifecycle()
                val ecoDrive by viewModel.ecoDrive.collectAsStateWithLifecycle()
                val mediaTrack by viewModel.mediaTrack.collectAsStateWithLifecycle()
                val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
                val isSimulationActive by viewModel.isSimulationActive.collectAsStateWithLifecycle()
                val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
                val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()

                val repoOwner by viewModel.repoOwnerInput.collectAsStateWithLifecycle()
                val repoName by viewModel.repoNameInput.collectAsStateWithLifecycle()
                val token by viewModel.tokenInput.collectAsStateWithLifecycle()

                // Richiesta permessi Bluetooth per Android 12+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Risultato permessi */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val permissions = arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                        val needed = permissions.filter {
                            ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                        }
                        if (needed.isNotEmpty()) {
                            permissionLauncher.launch(needed.toTypedArray())
                        }
                    }
                }

                // Gestione del tasto back di sistema per tornare all'HUD principale
                BackHandler(enabled = currentScreen != DashboardScreen.HUD) {
                    viewModel.selectScreen(DashboardScreen.HUD)
                }

                val configuration = LocalConfiguration.current
                val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AutomotiveBlack),
                    bottomBar = {
                        if (!isLandscape) {
                            NavigationBar(
                                containerColor = AutomotiveSurface,
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("automotive_bottom_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == DashboardScreen.HUD,
                                    onClick = { viewModel.selectScreen(DashboardScreen.HUD) },
                                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "HUD") },
                                    label = { Text("HUD") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == DashboardScreen.TRIP_COMPUTER,
                                    onClick = { viewModel.selectScreen(DashboardScreen.TRIP_COMPUTER) },
                                    icon = { Icon(Icons.Default.Route, contentDescription = "Trip") },
                                    label = { Text("Trip") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == DashboardScreen.ECO_DRIVE,
                                    onClick = { viewModel.selectScreen(DashboardScreen.ECO_DRIVE) },
                                    icon = { Icon(Icons.Default.Eco, contentDescription = "eco:Drive") },
                                    label = { Text("eco:Drive") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == DashboardScreen.MEDIA_PLAYER,
                                    onClick = { viewModel.selectScreen(DashboardScreen.MEDIA_PLAYER) },
                                    icon = { Icon(Icons.Default.MusicNote, contentDescription = "Media") },
                                    label = { Text("Media") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == DashboardScreen.SETTINGS,
                                    onClick = { viewModel.selectScreen(DashboardScreen.SETTINGS) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Impostazioni") },
                                    label = { Text("Setup") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // In modalità Landscape, usa la Navigation Rail stile Android Auto
                        if (isLandscape) {
                            NavigationRail(
                                containerColor = AutomotiveSurface,
                                header = {
                                    IconButton(
                                        onClick = { viewModel.selectScreen(DashboardScreen.HUD) },
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = "Logo",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .testTag("automotive_nav_rail")
                            ) {
                                NavigationRailItem(
                                    selected = currentScreen == DashboardScreen.HUD,
                                    onClick = { viewModel.selectScreen(DashboardScreen.HUD) },
                                    icon = { Icon(Icons.Default.Speed, contentDescription = "HUD") },
                                    label = { Text("HUD") }
                                )
                                NavigationRailItem(
                                    selected = currentScreen == DashboardScreen.TRIP_COMPUTER,
                                    onClick = { viewModel.selectScreen(DashboardScreen.TRIP_COMPUTER) },
                                    icon = { Icon(Icons.Default.Route, contentDescription = "Trip") },
                                    label = { Text("Trip") }
                                )
                                NavigationRailItem(
                                    selected = currentScreen == DashboardScreen.ECO_DRIVE,
                                    onClick = { viewModel.selectScreen(DashboardScreen.ECO_DRIVE) },
                                    icon = { Icon(Icons.Default.Eco, contentDescription = "eco:Drive") },
                                    label = { Text("eco:Drive") }
                                )
                                NavigationRailItem(
                                    selected = currentScreen == DashboardScreen.MEDIA_PLAYER,
                                    onClick = { viewModel.selectScreen(DashboardScreen.MEDIA_PLAYER) },
                                    icon = { Icon(Icons.Default.MusicNote, contentDescription = "Media") },
                                    label = { Text("Media") }
                                )
                                NavigationRailItem(
                                    selected = currentScreen == DashboardScreen.PHONE,
                                    onClick = { viewModel.selectScreen(DashboardScreen.PHONE) },
                                    icon = { Icon(Icons.Default.Phone, contentDescription = "Telefono") },
                                    label = { Text("Telefono") }
                                )
                                NavigationRailItem(
                                    selected = currentScreen == DashboardScreen.SETTINGS,
                                    onClick = { viewModel.selectScreen(DashboardScreen.SETTINGS) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Impostazioni") },
                                    label = { Text("Setup") }
                                )
                            }
                        }

                        // Schermata attiva
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(AutomotiveBlack)
                        ) {
                            when (currentScreen) {
                                DashboardScreen.HUD -> HudScreen(
                                    telemetry = telemetry,
                                    mediaTrack = mediaTrack,
                                    ecoScore = ecoDrive.ecoIndex,
                                    connectionState = connectionState,
                                    brand = selectedBrand,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onNextTrack = { viewModel.nextTrack() },
                                    onPrevTrack = { viewModel.previousTrack() },
                                    onSimulateSteeringButton = { viewModel.simulateButtonPress(it) },
                                    onNavigateToMedia = { viewModel.selectScreen(DashboardScreen.MEDIA_PLAYER) },
                                    onNavigateToEco = { viewModel.selectScreen(DashboardScreen.ECO_DRIVE) }
                                )
                                DashboardScreen.TRIP_COMPUTER -> TripComputerScreen(
                                    tripA = tripA,
                                    tripB = tripB,
                                    onResetTrip = { viewModel.resetTrip(it) }
                                )
                                DashboardScreen.ECO_DRIVE -> EcoDriveScreen(
                                    ecoState = ecoDrive,
                                    suggestedGear = telemetry.suggestedGear,
                                    currentGear = telemetry.currentGear
                                )
                                DashboardScreen.MEDIA_PLAYER -> MediaScreen(
                                    mediaTrack = mediaTrack,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onNextTrack = { viewModel.nextTrack() },
                                    onPrevTrack = { viewModel.previousTrack() },
                                    onToggleShuffle = { viewModel.toggleShuffle() },
                                    onToggleRepeat = { viewModel.toggleRepeat() }
                                )
                                DashboardScreen.PHONE -> PhoneScreen()
                                DashboardScreen.SETTINGS -> SettingsScreen(
                                    connectionState = connectionState,
                                    isSimulationActive = isSimulationActive,
                                    selectedBrand = selectedBrand,
                                    updateStatus = updateStatus,
                                    repoOwner = repoOwner,
                                    repoName = repoName,
                                    token = token,
                                    onToggleSimulation = { viewModel.toggleSimulation(it) },
                                    onSelectBrand = { viewModel.setVehicleBrand(it) },
                                    onSaveRepoConfig = { owner, repo, pat -> viewModel.updateRepoConfig(owner, repo, pat) },
                                    onCheckForUpdates = { viewModel.checkForUpdates() },
                                    onDownloadAndInstall = { url, ver -> viewModel.downloadAndInstallUpdate(url, ver) },
                                    onResetUpdateStatus = { viewModel.resetUpdateStatus() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
