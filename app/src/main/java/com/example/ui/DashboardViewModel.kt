package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothConnectionState
import com.example.data.VehicleBrand
import com.example.data.VehicleRepository
import com.example.model.*
import com.example.updater.AppUpdateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DashboardScreen(val title: String) {
    HUD("Car HUD"),
    TRIP_COMPUTER("Trip Computer"),
    ECO_DRIVE("eco:Drive"),
    MEDIA_PLAYER("USB Media"),
    PHONE("Telefono"),
    SETTINGS("Impostazioni")
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    val vehicleRepository = VehicleRepository(application)
    val updateRepository = AppUpdateRepository(application)

    private val _currentScreen = MutableStateFlow(DashboardScreen.HUD)
    val currentScreen: StateFlow<DashboardScreen> = _currentScreen

    val telemetry: StateFlow<VehicleTelemetry> = vehicleRepository.telemetry
    val tripA: StateFlow<TripData> = vehicleRepository.tripA
    val tripB: StateFlow<TripData> = vehicleRepository.tripB
    val ecoDrive: StateFlow<EcoDriveState> = vehicleRepository.ecoDrive
    val mediaTrack: StateFlow<MediaTrack> = vehicleRepository.mediaTrack
    val connectionState: StateFlow<BluetoothConnectionState> = vehicleRepository.connectionState
    val isSimulationActive: StateFlow<Boolean> = vehicleRepository.isSimulationActive
    val selectedBrand: StateFlow<VehicleBrand> = vehicleRepository.selectedBrand
    val lastSteeringKey: SharedFlow<SteeringWheelButton> = vehicleRepository.lastSteeringKey

    val updateStatus: StateFlow<UpdateStatus> = updateRepository.status

    private val _repoOwnerInput = MutableStateFlow(updateRepository.repoOwner)
    val repoOwnerInput: StateFlow<String> = _repoOwnerInput

    private val _repoNameInput = MutableStateFlow(updateRepository.repoName)
    val repoNameInput: StateFlow<String> = _repoNameInput

    private val _tokenInput = MutableStateFlow(updateRepository.personalAccessToken)
    val tokenInput: StateFlow<String> = _tokenInput

    init {
        // Controllo automatico aggiornamenti all'avvio in background
        viewModelScope.launch {
            updateRepository.checkForUpdates(currentVersion = "1.0.0")
        }

        // Ascolto comandi al volante per navigare la dashboard se premuto MENU o ESC
        viewModelScope.launch {
            vehicleRepository.lastSteeringKey.collect { button ->
                when (button) {
                    SteeringWheelButton.MENU_SOURCE -> {
                        // Cicla le schermate principali
                        val next = when (_currentScreen.value) {
                            DashboardScreen.HUD -> DashboardScreen.TRIP_COMPUTER
                            DashboardScreen.TRIP_COMPUTER -> DashboardScreen.ECO_DRIVE
                            DashboardScreen.ECO_DRIVE -> DashboardScreen.MEDIA_PLAYER
                            DashboardScreen.MEDIA_PLAYER -> DashboardScreen.PHONE
                            DashboardScreen.PHONE -> DashboardScreen.SETTINGS
                            DashboardScreen.SETTINGS -> DashboardScreen.HUD
                        }
                        _currentScreen.value = next
                    }
                    SteeringWheelButton.ESC_BACK -> {
                        _currentScreen.value = DashboardScreen.HUD
                    }
                    else -> {}
                }
            }
        }
    }

    fun selectScreen(screen: DashboardScreen) {
        _currentScreen.value = screen
    }

    fun toggleSimulation(active: Boolean) {
        vehicleRepository.setSimulationMode(active)
    }

    fun setVehicleBrand(brand: VehicleBrand) {
        vehicleRepository.setVehicleBrand(brand)
    }

    fun togglePlayPause() = vehicleRepository.togglePlayPause()
    fun nextTrack() = vehicleRepository.nextTrack()
    fun previousTrack() = vehicleRepository.previousTrack()
    fun toggleShuffle() = vehicleRepository.toggleShuffle()
    fun toggleRepeat() = vehicleRepository.toggleRepeat()
    fun resetTrip(name: String) = vehicleRepository.resetTrip(name)
    fun simulateButtonPress(button: SteeringWheelButton) = vehicleRepository.simulateButtonPress(button)

    fun updateRepoConfig(owner: String, repo: String, token: String) {
        _repoOwnerInput.value = owner
        _repoNameInput.value = repo
        _tokenInput.value = token
        updateRepository.repoOwner = owner
        updateRepository.repoName = repo
        updateRepository.personalAccessToken = token
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            updateRepository.checkForUpdates(currentVersion = "1.0.0")
        }
    }

    fun downloadAndInstallUpdate(url: String, version: String) {
        viewModelScope.launch {
            updateRepository.downloadAndInstall(url, version)
        }
    }

    fun resetUpdateStatus() {
        updateRepository.resetStatus()
    }
}
