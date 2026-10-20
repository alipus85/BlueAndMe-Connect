package com.example.handlers

import com.example.protocol.BlueAndMeConstants
import com.example.protocol.ReflectionMessage
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class IgnitionState(val label: String) {
    STOP("STOP (Spento)"),
    MAR("MAR (Quadro Acceso)"),
    AVV("AVV (Avviamento / Motore Acceso)"),
    UNKNOWN("Sconosciuto")
}

data class VehicleStatusPayload(
    val ignitionState: IgnitionState,
    val isExternalPowerConnected: Boolean,
    val batteryVoltage: Float
)

/**
 * Gestore dell'interfaccia iVehicleStatus (0x25) per monitorare lo stato del quadro
 * e l'alimentazione della vettura.
 */
class VehicleStatusHandler(
    private val onStatusUpdate: (VehicleStatusPayload) -> Unit
) {
    fun handle(message: ReflectionMessage) {
        if (message.iid != BlueAndMeConstants.IID.VEHICLE_STATUS) return

        if (message.payload.size >= 3) {
            val buf = ByteBuffer.wrap(message.payload).order(ByteOrder.LITTLE_ENDIAN)
            val ignitionRaw = buf.get().toInt() and 0xFF
            val powerFlags = buf.get().toInt() and 0xFF
            val voltageRaw = if (buf.remaining() >= 2) buf.short.toInt() and 0xFFFF else 1420

            val ignition = when (ignitionRaw) {
                0x00 -> IgnitionState.STOP
                0x01 -> IgnitionState.MAR
                0x02 -> IgnitionState.AVV
                else -> IgnitionState.MAR
            }

            onStatusUpdate(
                VehicleStatusPayload(
                    ignitionState = ignition,
                    isExternalPowerConnected = (powerFlags and 0x01) == 1,
                    batteryVoltage = voltageRaw / 100.0f
                )
            )
        }
    }
}
