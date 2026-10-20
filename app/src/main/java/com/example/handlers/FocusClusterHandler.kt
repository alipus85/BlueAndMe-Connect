package com.example.handlers

import com.example.protocol.BlueAndMeConstants
import com.example.protocol.ReflectionMessage
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gestore dell'interfaccia iFocus (0x46) per pilotare il display a matrice
 * del quadro strumenti (cluster display) della vettura tramite i template FCS0.
 */
class FocusClusterHandler(
    private val onTemplateRequested: (Int) -> Unit = {}
) {
    fun handle(message: ReflectionMessage) {
        if (message.iid != BlueAndMeConstants.IID.FOCUS) return

        when (message.fid) {
            BlueAndMeConstants.FID_FOCUS.SET_TEMPLATE -> {
                if (message.payload.size >= 2) {
                    val buf = ByteBuffer.wrap(message.payload).order(ByteOrder.LITTLE_ENDIAN)
                    val templateId = buf.short.toInt() and 0xFFFF
                    onTemplateRequested(templateId)
                }
            }
        }
    }

    /**
     * Attiva un template specifico sul quadro strumenti (es. 100 per Car Menu).
     */
    fun createSetTemplateMessage(templateId: Int): ReflectionMessage {
        val payload = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(templateId.toShort())
            .array()
        return ReflectionMessage(
            iid = BlueAndMeConstants.IID.FOCUS,
            fid = BlueAndMeConstants.FID_FOCUS.SET_TEMPLATE,
            payload = payload
        )
    }

    /**
     * Imposta il testo di una proprietà (es. Field 1..5, Value 1..5 in Template 100).
     */
    fun createSetPropertyMessage(propertyId: Int, text: String): ReflectionMessage {
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val payload = ByteBuffer.allocate(2 + 2 + textBytes.size).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(propertyId.toShort())
            .putShort(textBytes.size.toShort())
            .put(textBytes)
            .array()
        return ReflectionMessage(
            iid = BlueAndMeConstants.IID.FOCUS,
            fid = BlueAndMeConstants.FID_FOCUS.SET_PROPERTY,
            payload = payload
        )
    }

    /**
     * Invia il comando di refresh per applicare le modifiche al display vettura.
     */
    fun createUpdateScreenMessage(): ReflectionMessage {
        return ReflectionMessage(
            iid = BlueAndMeConstants.IID.FOCUS,
            fid = BlueAndMeConstants.FID_FOCUS.UPDATE_SCREEN,
            payload = ByteArray(0)
        )
    }

    /**
     * Crea l'avviso di riserva carburante con km rimanenti (Template 250).
     */
    fun createLowFuelWarningMessage(kmRemaining: Int): ReflectionMessage {
        val text = "$kmRemaining km"
        return createSetPropertyMessage(1, text)
    }
}
