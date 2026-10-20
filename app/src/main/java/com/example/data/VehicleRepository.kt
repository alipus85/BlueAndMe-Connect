package com.example.data

import android.content.Context
import com.example.bluetooth.BluetoothConnectionState
import com.example.bluetooth.BluetoothSppManager
import com.example.handlers.CanSensorPayload
import com.example.handlers.FocusClusterHandler
import com.example.handlers.HidReportHandler
import com.example.handlers.PositioningSensorHandler
import com.example.handlers.VehicleStatusHandler
import com.example.handlers.VehicleStatusPayload
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class VehicleBrand(val brandName: String, val tagline: String) {
    FIAT("FIAT", "Blue&Me Connected"),
    ALFA_ROMEO("Alfa Romeo", "Cuore Sportivo"),
    LANCIA("Lancia", "Eleganza in Movimento"),
    ABARTH("Abarth", "Scorpion Telemetry")
}

class VehicleRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val simulator = SimulatedBlueAndMeDataSource()

    private val _isSimulationActive = MutableStateFlow(true)
    val isSimulationActive: StateFlow<Boolean> = _isSimulationActive

    private val _selectedBrand = MutableStateFlow(VehicleBrand.FIAT)
    val selectedBrand: StateFlow<VehicleBrand> = _selectedBrand

    private val _lastSteeringKey = MutableSharedFlow<SteeringWheelButton>(extraBufferCapacity = 8)
    val lastSteeringKey: SharedFlow<SteeringWheelButton> = _lastSteeringKey

    private val _liveTelemetry = MutableStateFlow(VehicleTelemetry())
    val telemetry: StateFlow<VehicleTelemetry> = combine(
        _isSimulationActive,
        simulator.telemetry,
        _liveTelemetry
    ) { isSim, simData, liveData ->
        if (isSim) simData else liveData
    }.stateIn(scope, SharingStarted.Eagerly, VehicleTelemetry())

    val tripA: StateFlow<TripData> = simulator.tripA
    val tripB: StateFlow<TripData> = simulator.tripB
    val ecoDrive: StateFlow<EcoDriveState> = simulator.ecoDrive
    val mediaTrack: StateFlow<MediaTrack> = simulator.mediaTrack

    val hidHandler = HidReportHandler { button, _ ->
        scope.launch {
            _lastSteeringKey.emit(button)
            when (button) {
                SteeringWheelButton.ARROW_LEFT -> simulator.previousTrack()
                SteeringWheelButton.ARROW_RIGHT -> simulator.nextTrack()
                SteeringWheelButton.OK_ENTER -> simulator.togglePlayPause()
                else -> {}
            }
        }
    }

    val sensorHandler = PositioningSensorHandler { canData: CanSensorPayload ->
        _liveTelemetry.value = _liveTelemetry.value.copy(
            speedKmH = canData.speedKmH,
            isReverseEngaged = canData.isReverseEngaged,
            odometerMeters = _liveTelemetry.value.odometerMeters + canData.odometerDeltaMeters
        )
    }

    val vehicleStatusHandler = VehicleStatusHandler { status: VehicleStatusPayload ->
        _liveTelemetry.value = _liveTelemetry.value.copy(
            batteryVoltage = status.batteryVoltage
        )
    }

    val focusClusterHandler = FocusClusterHandler()

    val bluetoothSppManager = BluetoothSppManager(context) { message ->
        hidHandler.handle(message)
        sensorHandler.handle(message)
        vehicleStatusHandler.handle(message)
        focusClusterHandler.handle(message)
    }

    val connectionState: StateFlow<BluetoothConnectionState> = bluetoothSppManager.connectionState

    init {
        // Avvia il simulatore per rendere la dashboard immediatamente viva e reattiva
        simulator.start()

        scope.launch {
            simulator.steeringEvents.collect { button ->
                _lastSteeringKey.emit(button)
            }
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        _isSimulationActive.value = enabled
        if (enabled) {
            simulator.start()
        } else {
            simulator.stop()
        }
    }

    fun setVehicleBrand(brand: VehicleBrand) {
        _selectedBrand.value = brand
    }

    fun togglePlayPause() = simulator.togglePlayPause()
    fun nextTrack() = simulator.nextTrack()
    fun previousTrack() = simulator.previousTrack()
    fun toggleShuffle() = simulator.toggleShuffle()
    fun toggleRepeat() = simulator.toggleRepeat()
    fun resetTrip(name: String) = simulator.resetTrip(name)
    fun simulateButtonPress(button: SteeringWheelButton) = simulator.triggerSteeringButton(button)
}
