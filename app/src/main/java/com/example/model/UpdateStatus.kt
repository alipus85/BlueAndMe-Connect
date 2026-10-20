package com.example.model

import java.io.File

/**
 * Stati del ciclo di vita dell'aggiornamento da GitHub Actions (stile CrealityAuto).
 */
sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    data class NoUpdateAvailable(val currentVersion: String) : UpdateStatus
    data class UpdateAvailable(
        val latestVersion: String,
        val releaseName: String,
        val changelog: String,
        val downloadUrl: String,
        val apkSize: Long
    ) : UpdateStatus
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateStatus
    data class ReadyToInstall(val apkFile: File, val version: String) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
