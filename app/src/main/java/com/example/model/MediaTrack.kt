package com.example.model

/**
 * Modello multimediale per il lettore USB Blue&Me.
 */
data class MediaTrack(
    val title: String = "A Day Without Rain",
    val artist: String = "Enya",
    val album: String = "A Day Without Rain",
    val folderName: String = "Chillout Essentials",
    val durationSeconds: Int = 188,
    val positionSeconds: Int = 42,
    val isPlaying: Boolean = true,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val autoplayOnStart: Boolean = true
) {
    val progressFraction: Float
        get() = if (durationSeconds > 0) positionSeconds.toFloat() / durationSeconds else 0f

    val positionFormatted: String
        get() = String.format("%d:%02d", positionSeconds / 60, positionSeconds % 60)

    val durationFormatted: String
        get() = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60)
}
