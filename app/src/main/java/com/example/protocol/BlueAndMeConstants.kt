package com.example.protocol

import java.util.UUID

/**
 * Costanti ufficiali del protocollo TomTom Blue&Me estratte da ttsystem e carlinkd.
 */
object BlueAndMeConstants {
    // UUID standard Serial Port Profile (SPP / RFCOMM)
    val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Nomi Bluetooth tipici della centralina veicolo
    const val DEVICE_NAME_PREFIX = "Blue&Me"
    const val TOMTOM_DEVICE_NAME = "Blue&Me TomTom"

    // Byte speciali per framing SLIP (IOF_UART_SLIP)
    const val SLIP_END: Byte = 0xC0.toByte()
    const val SLIP_ESC: Byte = 0xDB.toByte()
    const val SLIP_ESC_END: Byte = 0xDC.toByte()
    const val SLIP_ESC_ESC: Byte = 0xDD.toByte()

    // Interface ID (IID) a 16-bit estratti dal demone carlinkd
    object IID {
        const val INTERFACE_PUBLICATION: Int = 0x0001
        const val POSITIONING_SENSOR: Int = 0x0022    // Telemetria CAN-bus (Velocità 10Hz, odometro, retro)
        const val VEHICLE_STATUS: Int = 0x0025        // Stato chiave accensione (STOP, MAR, AVV), alimentazione
        const val HMI_REMOTER: Int = 0x0032
        const val BASIC_PLANNER: Int = 0x0034
        const val NAV_OBJECT: Int = 0x0036
        const val EXTERNAL_GUIDANCE_ADVICE: Int = 0x0037 // Frecce quadro strumenti
        const val AUTHENTICATION: Int = 0x0043        // Handshake & challenge
        const val FEATURE: Int = 0x0044
        const val CALL_CONTROL: Int = 0x0045          // Gestione chiamate e vivavoce
        const val FOCUS: Int = 0x0046                 // Gestione display matrice cruscotto e template FCS0
        const val LOCATION: Int = 0x0047
        const val LOCATION_CATEGORY: Int = 0x0048
        const val LOCATION_STORAGE: Int = 0x0049
        const val ROUTE: Int = 0x004A
        const val ROUTE_INSTRUCTION: Int = 0x004B
        const val HID_REPORT: Int = 0x004D            // Comandi al Volante fisici vettura
        const val ROUTE_CONSTRUCTOR: Int = 0x004E
        const val SETTING: Int = 0x0050               // Sincronizzazione preferenze veicolo
        const val THROUGHPUT_TEST: Int = 0x00FE
        const val CARLINK: Int = 0x0100
    }

    // Function ID (FID) per i comandi al volante (iHidReport 0x4D)
    object FID_HID {
        const val DEVICE_AVAILABLE = 0x01
        const val DEVICE_UNAVAILABLE = 0x02
        const val REPORT = 0x03
    }

    // Function ID (FID) per telemetria veicolo (iPositioningSensor 0x22)
    object FID_SENSOR {
        const val SUBSCRIBE = 0x01
        const val CONFIGURATION = 0x02
        const val SENSOR_DATA = 0x03
    }

    // Function ID (FID) per cluster cruscotto (iFocus 0x46)
    object FID_FOCUS {
        const val INIT = 0x01
        const val REQUEST_FULL_MODE = 0x05
        const val RELEASE_FULL_MODE = 0x06
        const val SET_TEMPLATE = 0x0B
        const val SET_HOST_TEMPLATE = 0x0C
        const val SET_PROPERTY = 0x0D
        const val SET_INDEXED_PROPERTY = 0x0E
        const val RAISE_EVENT = 0x0F
        const val UPDATE_SCREEN = 0x11
    }

    // Template IDs estratti dai file binari FCS0 (focus/*.fba)
    object Templates {
        const val CAR_MENU = 100            // Template 100: Car Menu a 5 campi e 5 valori
        const val PIN_PAIRING = 3           // Template 3: Dialogo PIN a 4 cifre
        const val CAR_MENU_PAGED = 113      // Template 113: Car Menu 1 of 1
        const val HEAD_UNIT_SELECT = 255    // Template 255: Selezione Head Unit Blue&Me
        const val CONNECT_TO_CAR = 244      // Template 244: Connessione auto
        const val SMS_READER = 247          // Template 247: Notifica SMS
        const val UNIT_ALIGNMENT = 248      // Template 248: Allineamento unità e orologio
        const val LOW_FUEL_WARNING = 250    // Template 250: Allarme riserva carburante con km residui
        const val NEAREST_FUEL = 251        // Template 251: Stazioni di servizio
    }

    // Codici tasti al volante estratti da CFmHidReport::OnReport (0x0005da3c)
    object SteeringKeys {
        const val ESC_BACK = 0x01
        const val ARROW_UP = 0x02
        const val ARROW_DOWN = 0x03
        const val ARROW_LEFT = 0x04
        const val OK_ENTER = 0x05
        const val PHONE = 0x18
        const val MENU_SOURCE = 0x1A
        const val VOICE_PTT = 0x1D
    }
}
