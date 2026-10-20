package com.example.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Rappresenta un pacchetto del protocollo RPC Reflection con header a 3 byte:
 * - iid: Interface ID (uint16, 2 byte in Little-Endian)
 * - fid: Function ID (uint8, 1 byte)
 * - payload: argomenti binari serializzati
 */
data class ReflectionMessage(
    val iid: Int,
    val fid: Int,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReflectionMessage
        if (iid != other.iid) return false
        if (fid != other.fid) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iid
        result = 31 * result + fid
        result = 31 * result + payload.contentHashCode()
        return result
    }

    companion object {
        fun parse(frame: ByteArray): ReflectionMessage? {
            if (frame.size < 3) return null

            val buffer = ByteBuffer.wrap(frame).order(ByteOrder.LITTLE_ENDIAN)
            val iid = buffer.short.toInt() and 0xFFFF
            val fid = buffer.get().toInt() and 0xFF

            val payloadLength = frame.size - 3
            val payload = ByteArray(payloadLength)
            buffer.get(payload)

            return ReflectionMessage(iid, fid, payload)
        }

        fun serialize(iid: Int, fid: Int, payload: ByteArray = ByteArray(0)): ByteArray {
            val buffer = ByteBuffer.allocate(3 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
            buffer.putShort((iid and 0xFFFF).toShort())
            buffer.put((fid and 0xFF).toByte())
            buffer.put(payload)
            return buffer.array()
        }
    }
}
