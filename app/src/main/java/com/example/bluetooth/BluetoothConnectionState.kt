package com.example.bluetooth

sealed interface BluetoothConnectionState {
    object Disconnected : BluetoothConnectionState
    object Scanning : BluetoothConnectionState
    data class Connecting(val deviceName: String, val address: String) : BluetoothConnectionState
    data class Connected(val deviceName: String, val address: String) : BluetoothConnectionState
    data class PairingRequired(val pinSuggested: String = "1234") : BluetoothConnectionState
    data class Error(val message: String) : BluetoothConnectionState
}
