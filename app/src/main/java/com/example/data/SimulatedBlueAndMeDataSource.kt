package com.example.data

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.sin

/**
 * Sorgente dati simulata ad alta fedeltà che replica la dinamica di un'auto Fiat/Alfa con Blue&Me
 * per testare immediatamente la dashboard senza collegamento fisico al veicolo.
 */
class SimulatedBlueAndMeDataSource {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var simulationJob: Job? = null

    private val _telemetry = MutableStateFlow(VehicleTelemetry())
    val telemetry: StateFlow<VehicleTelemetry> = _telemetry

    private val _tripA = MutableStateFlow(
        TripData(
            name = "Trip A",
            distanceKm = 42.6f,
            avgSpeedKmH = 58.4f,
            durationMinutes = 44,
            avgConsumptionL100Km = 5.9f,
            instantConsumptionL100Km = 5.4f
        )
    )
    val tripA: StateFlow<TripData> = _tripA

    private val _tripB = MutableStateFlow(
        TripData(
            name = "Trip B",
            distanceKm = 312.0f,
            avgSpeedKmH = 64.2f,
            durationMinutes = 290,
            avgConsumptionL100Km = 6.3f,
            instantConsumptionL100Km = 5.4f
        )
    )
    val tripB: StateFlow<TripData> = _tripB

    private val _ecoDrive = MutableStateFlow(EcoDriveState())
    val ecoDrive: StateFlow<EcoDriveState> = _ecoDrive

    private val _mediaTrack = MutableStateFlow(MediaTrack())
    val mediaTrack: StateFlow<MediaTrack> = _mediaTrack

    private val _steeringEvents = MutableSharedFlow<SteeringWheelButton>(extraBufferCapacity = 8)
    val steeringEvents: SharedFlow<SteeringWheelButton> = _steeringEvents

    private val playlist = listOf(
        MediaTrack("A Day Without Rain", "Enya", "A Day Without Rain", "New Age", 188, 45),
        MediaTrack("Only Time", "Enya", "A Day Without Rain", "New Age", 215, 0),
        MediaTrack("Viva La Vida", "Coldplay", "Viva La Vida", "Rock Pop", 242, 12),
        MediaTrack("Enjoy the Silence", "Depeche Mode", "Violator", "Synthwave", 255, 60),
        MediaTrack("Con Te Partirò", "Andrea Bocelli", "Romanza", "Classica", 249, 105)
    )
    private var playlistIndex = 0

    var isSimulating: Boolean = false
        private set

    fun start() {
        if (isSimulating) return
        isSimulating = true
        simulationJob = scope.launch {
            var step = 0.0
            while (isActive && isSimulating) {
                delay(100) // 10 Hz come il protocollo CAN Blue&Me reale
                step += 0.05

                // Curva dinamica di velocità tra 45 e 110 km/h
                val targetSpeed = (75.0 + 32.0 * sin(step * 0.4) + 10.0 * sin(step * 0.9)).coerceIn(0.0, 140.0).toFloat()
                
                // Calcolo marcia e RPM realistici
                val gear = when {
                    targetSpeed < 18f -> 1
                    targetSpeed < 35f -> 2
                    targetSpeed < 55f -> 3
                    targetSpeed < 75f -> 4
                    targetSpeed < 98f -> 5
                    else -> 6
                }

                val ratio = when (gear) {
                    1 -> 42f
                    2 -> 35f
                    3 -> 30f
                    4 -> 28f
                    5 -> 25f
                    else -> 22f
                }
                val calculatedRpm = ((targetSpeed * ratio) + 950f).coerceIn(850f, 5200f).toInt()

                // Gear Shift Indicator (GSI)
                val suggested = when {
                    calculatedRpm > 2500 && gear < 6 -> gear + 1
                    calculatedRpm < 1250 && gear > 1 -> gear - 1
                    else -> null
                }

                val instantCons = (4.2f + (calculatedRpm / 1200f) * 1.5f + (sin(step.toFloat()) * 0.8f)).coerceIn(2.0f, 16.0f)

                _telemetry.value = _telemetry.value.copy(
                    speedKmH = targetSpeed,
                    rpm = calculatedRpm,
                    currentGear = gear,
                    suggestedGear = suggested,
                    coolantTempC = 90,
                    fuelPercent = 68,
                    fuelRangeKm = (420 - (step * 0.02)).toInt().coerceAtLeast(30),
                    batteryVoltage = 14.2f + (sin(step.toFloat() * 2) * 0.15f),
                    isReverseEngaged = false,
                    isLowFuelWarning = false,
                    odometerMeters = _telemetry.value.odometerMeters + (targetSpeed * 100 / 36).toLong()
                )

                // Avanzamento traccia multimediale ogni secondo
                if ((step * 10).toLong() % 10 == 0L && _mediaTrack.value.isPlaying) {
                    val currentPos = _mediaTrack.value.positionSeconds + 1
                    if (currentPos >= _mediaTrack.value.durationSeconds) {
                        nextTrack()
                    } else {
                        _mediaTrack.value = _mediaTrack.value.copy(positionSeconds = currentPos)
                    }
                }
            }
        }
    }

    fun stop() {
        isSimulating = false
        simulationJob?.cancel()
        simulationJob = null
    }

    fun triggerSteeringButton(button: SteeringWheelButton) {
        scope.launch {
            _steeringEvents.emit(button)
            when (button) {
                SteeringWheelButton.ARROW_LEFT -> previousTrack()
                SteeringWheelButton.ARROW_RIGHT -> nextTrack()
                SteeringWheelButton.OK_ENTER -> togglePlayPause()
                else -> {}
            }
        }
    }

    fun togglePlayPause() {
        val current = _mediaTrack.value
        _mediaTrack.value = current.copy(isPlaying = !current.isPlaying)
    }

    fun nextTrack() {
        playlistIndex = (playlistIndex + 1) % playlist.size
        _mediaTrack.value = playlist[playlistIndex].copy(isPlaying = true, positionSeconds = 0)
    }

    fun previousTrack() {
        playlistIndex = if (playlistIndex - 1 < 0) playlist.size - 1 else playlistIndex - 1
        _mediaTrack.value = playlist[playlistIndex].copy(isPlaying = true, positionSeconds = 0)
    }

    fun toggleShuffle() {
        _mediaTrack.value = _mediaTrack.value.copy(isShuffle = !_mediaTrack.value.isShuffle)
    }

    fun toggleRepeat() {
        _mediaTrack.value = _mediaTrack.value.copy(isRepeat = !_mediaTrack.value.isRepeat)
    }

    fun resetTrip(tripName: String) {
        if (tripName.contains("A", ignoreCase = true)) {
            _tripA.value = TripData("Trip A", 0f, 0f, 0, 0f, 0f)
        } else {
            _tripB.value = TripData("Trip B", 0f, 0f, 0, 0f, 0f)
        }
    }
}
