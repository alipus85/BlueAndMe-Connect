package com.example.model

/**
 * Modello per i dati del Trip Computer (Trip A e Trip B).
 */
data class TripData(
    val name: String,
    val distanceKm: Float = 0f,
    val avgSpeedKmH: Float = 0f,
    val durationMinutes: Int = 0,
    val avgConsumptionL100Km: Float = 6.2f,
    val instantConsumptionL100Km: Float = 5.8f
) {
    val durationFormatted: String
        get() {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            return String.format("%02d:%02d", hours, mins)
        }
}
