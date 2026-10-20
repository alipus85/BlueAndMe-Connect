package com.example.model

/**
 * Modello unificato per i parametri di bordo ricevuti da Blue&Me / CAN-bus.
 */
data class VehicleTelemetry(
    val speedKmH: Float = 0f,
    val rpm: Int = 850,
    val currentGear: Int = 1,
    val suggestedGear: Int? = null,        // GSI: Gear Shift Indicator (null, o marcia consigliata)
    val coolantTempC: Int = 90,
    val fuelPercent: Int = 68,
    val fuelRangeKm: Int = 420,
    val batteryVoltage: Float = 14.2f,
    val isReverseEngaged: Boolean = false,
    val odometerMeters: Long = 124500000L,
    val isLowFuelWarning: Boolean = false,
    val fuelType: FuelType = FuelType.BENZINA,
    val timestamp: Long = System.currentTimeMillis()
)

enum class FuelType(val label: String) {
    BENZINA("Benzina"),
    DIESEL("Diesel"),
    GPL("GPL"),
    METANO("Metano")
}
