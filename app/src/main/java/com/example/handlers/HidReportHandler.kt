package com.example.handlers

import com.example.model.SteeringWheelButton
import com.example.protocol.BlueAndMeConstants
import com.example.protocol.ReflectionMessage
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gestore dell'interfaccia iHidReport (0x4D) per i comandi al volante.
 */
class HidReportHandler(
    private val onButtonAction: (SteeringWheelButton, Int) -> Unit
) {
    fun handle(message: ReflectionMessage) {
        if (message.iid != BlueAndMeConstants.IID.HID_REPORT) return

        when (message.fid) {
            BlueAndMeConstants.FID_HID.REPORT -> {
                if (message.payload.size >= 4) {
                    val buf = ByteBuffer.wrap(message.payload).order(ByteOrder.LITTLE_ENDIAN)
                    val requestId = buf.short.toInt() and 0xFFFF
                    val rawKey = buf.short.toInt() and 0xFFFF

                    val button = SteeringWheelButton.fromRawCode(rawKey)
                    onButtonAction(button, requestId)
                }
            }
        }
    }
}
