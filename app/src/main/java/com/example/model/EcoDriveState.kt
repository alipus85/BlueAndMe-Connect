package com.example.model

/**
 * Modello dati per l'analisi dello stile di guida eco:Drive.
 */
data class EcoDriveState(
    val ecoIndex: Int = 82,                // Indice da 0 a 100
    val accelerationScore: Int = 85,       // Gradualità acceleratore
    val decelerationScore: Int = 80,       // Frenata rigenerativa/anticipo
    val gearShiftScore: Int = 88,          // Tempestività cambio marcia
    val speedSteadinessScore: Int = 78,    // Costanza andatura
    val co2GramsPerKm: Int = 112,          // Emissioni stimate g/km
    val fuelSavedLiters: Float = 1.4f,     // Litri risparmiati stimati
    val realTimeAdvice: String = "Stile ecologico ottimale. Mantieni andatura costante."
)
