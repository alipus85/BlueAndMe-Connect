package com.example.handlers

import com.example.protocol.BlueAndMeConstants
import com.example.protocol.ReflectionMessage
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Dati grezzi estratti dal frame SensorData CAN (0x22).
 */
data class CanSensorPayload(
    val speedKmH: Float,
    val isReverseEngaged: Boolean,
    val statusFlags: Int,
    val odometerDeltaMeters: Int
)

/**
 * Gestore dell'interfaccia iPositioningSensor (0x22) per telemetria CAN (velocità, retromarcia, odometro).
 */
class PositioningSensorHandler(
    private val onSensorData: (CanSensorPayload) -> Unit
) {
    fun handle(message: ReflectionMessage) {
        if (message.iid != BlueAndMeConstants.IID.POSITIONING_SENSOR) return

        when (message.fid) {
            BlueAndMeConstants.FID_SENSOR.SENSOR_DATA -> {
                if (message.payload.size >= 8) {
                    val buf = ByteBuffer.wrap(message.payload).order(ByteOrder.LITTLE_ENDIAN)
                    val rawSpeed = buf.short.toInt() and 0xFFFF
                    val speedKmH = rawSpeed / 100.0f
                    val reverseByte = buf.get().toInt() and 0xFF
                    val isReverse = (reverseByte and 0x01) == 1
                    val statusFlags = buf.get().toInt() and 0xFF
                    val odometerDelta = buf.int

                    onSensorData(
                        CanSensorPayload(
                            speedKmH = speedKmH,
                            isReverseEngaged = isReverse,
                            statusFlags = statusFlags,
                            odometerDeltaMeters = odometerDelta
                        )
                    )
                }
            }
        }
    }

    /**
     * Crea il pacchetto Subscribe (fid = 0x01) con frequenza richiesta di 100ms (10 Hz).
     */
    fun createSubscribePacket(intervalMs: Int = 100): ReflectionMessage {
        val payload = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(intervalMs)
            .array()
        return ReflectionMessage(
            iid = BlueAndMeConstants.IID.POSITIONING_SENSOR,
            fid = BlueAndMeConstants.FID_SENSOR.SUBSCRIBE,
            payload = payload
        )
    }
}
