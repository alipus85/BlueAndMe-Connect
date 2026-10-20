package com.example.protocol

import java.io.ByteArrayOutputStream

/**
 * Codificatore e decodificatore per il framing SLIP (Serial Line Internet Protocol)
 * con delimitatori 0xC0 e sequenze di escape 0xDB.
 */
class SlipFrameCodec {
    private val buffer = ByteArrayOutputStream()
    private var inEscape = false

    /**
     * Alimenta il decodificatore con byte ricevuti da socket RFCOMM seriale
     * ed emette frame completi ogni volta che incontra il delimitatore 0xC0.
     */
    @Synchronized
    fun feed(data: ByteArray, length: Int, onFrameReceived: (ByteArray) -> Unit) {
        for (i in 0 until length) {
            val b = data[i]
            if (inEscape) {
                when (b) {
                    BlueAndMeConstants.SLIP_ESC_END -> buffer.write(BlueAndMeConstants.SLIP_END.toInt())
                    BlueAndMeConstants.SLIP_ESC_ESC -> buffer.write(BlueAndMeConstants.SLIP_ESC.toInt())
                    else -> buffer.write(b.toInt())
                }
                inEscape = false
            } else if (b == BlueAndMeConstants.SLIP_ESC) {
                inEscape = true
            } else if (b == BlueAndMeConstants.SLIP_END) {
                if (buffer.size() > 0) {
                    onFrameReceived(buffer.toByteArray())
                    buffer.reset()
                }
            } else {
                buffer.write(b.toInt())
            }
        }
    }

    /**
     * Codifica un pacchetto binario serializzato in un frame SLIP racchiuso tra 0xC0.
     */
    fun encode(payload: ByteArray): ByteArray {
        val out = ByteArrayOutputStream(payload.size + 16)
        out.write(BlueAndMeConstants.SLIP_END.toInt())
        for (b in payload) {
            when (b) {
                BlueAndMeConstants.SLIP_END -> {
                    out.write(BlueAndMeConstants.SLIP_ESC.toInt())
                    out.write(BlueAndMeConstants.SLIP_ESC_END.toInt())
                }
                BlueAndMeConstants.SLIP_ESC -> {
                    out.write(BlueAndMeConstants.SLIP_ESC.toInt())
                    out.write(BlueAndMeConstants.SLIP_ESC_ESC.toInt())
                }
                else -> out.write(b.toInt())
            }
        }
        out.write(BlueAndMeConstants.SLIP_END.toInt())
        return out.toByteArray()
    }
}
