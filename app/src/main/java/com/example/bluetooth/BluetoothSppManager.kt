package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.protocol.BlueAndMeConstants
import com.example.protocol.ReflectionMessage
import com.example.protocol.SlipFrameCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Gestore del canale di comunicazione seriale Bluetooth SPP (RFCOMM) con la centralina Blue&Me.
 */
class BluetoothSppManager(
    private val context: Context,
    private val onMessageReceived: (ReflectionMessage) -> Unit
) {
    private val _connectionState = MutableStateFlow<BluetoothConnectionState>(BluetoothConnectionState.Disconnected)
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private val codec = SlipFrameCodec()
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        scope.launch {
            disconnect()
            _connectionState.value = BluetoothConnectionState.Connecting(
                deviceName = device.name ?: "Blue&Me Device",
                address = device.address
            )

            try {
                val newSocket = device.createRfcommSocketToServiceRecord(BlueAndMeConstants.SPP_UUID)
                newSocket.connect()
                socket = newSocket
                inputStream = newSocket.inputStream
                outputStream = newSocket.outputStream
                isRunning = true

                _connectionState.value = BluetoothConnectionState.Connected(
                    deviceName = device.name ?: "Blue&Me",
                    address = device.address
                )

                listenForIncomingData()
            } catch (e: Exception) {
                _connectionState.value = BluetoothConnectionState.Error("Connessione fallita: ${e.localizedMessage ?: "Errore socket"}")
                disconnect()
            }
        }
    }

    private suspend fun listenForIncomingData() = withContext(Dispatchers.IO) {
        val buffer = ByteArray(1024)
        while (isRunning) {
            try {
                val bytesRead = inputStream?.read(buffer) ?: -1
                if (bytesRead > 0) {
                    codec.feed(buffer, bytesRead) { frame ->
                        val msg = ReflectionMessage.parse(frame)
                        if (msg != null) {
                            onMessageReceived(msg)
                        }
                    }
                } else if (bytesRead == -1) {
                    break
                }
            } catch (e: Exception) {
                break
            }
        }
        disconnect()
    }

    fun send(message: ReflectionMessage) {
        scope.launch {
            try {
                val serialized = ReflectionMessage.serialize(message.iid, message.fid, message.payload)
                val encoded = codec.encode(serialized)
                outputStream?.write(encoded)
                outputStream?.flush()
            } catch (e: Exception) {
                // Gestione disconnessione o errore di scrittura
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun findPairedBlueAndMeDevice(): BluetoothDevice? {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
        return try {
            adapter.bondedDevices.firstOrNull {
                it.name?.contains(BlueAndMeConstants.DEVICE_NAME_PREFIX, ignoreCase = true) == true
            }
        } catch (e: Exception) {
            null
        }
    }

    fun disconnect() {
        isRunning = false
        try { inputStream?.close() } catch (_: Exception) {}
        try { outputStream?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        inputStream = null
        outputStream = null
        if (_connectionState.value !is BluetoothConnectionState.Error) {
            _connectionState.value = BluetoothConnectionState.Disconnected
        }
    }
}
