package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.UpdateStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Gestore degli aggiornamenti continui dell'app da GitHub Actions / Releases (stile CrealityAuto).
 * Supporta repository sia pubblici che privati tramite Personal Access Token (PAT).
 */
class AppUpdateRepository(private val context: Context) {

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status

    var repoOwner: String = "alipus85"
    var repoName: String = "BlueAndMe-Connect"
    var personalAccessToken: String = ""

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val gitHubService = retrofit.create(GitHubReleaseService::class.java)

    suspend fun checkForUpdates(currentVersion: String = "1.0.0") = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Checking
        try {
            val authHeader = if (personalAccessToken.isNotBlank()) "Bearer $personalAccessToken" else null
            val response = gitHubService.getLatestRelease(
                owner = repoOwner.trim(),
                repo = repoName.trim(),
                authHeader = authHeader
            )

            if (response.isSuccessful) {
                val release = response.body()
                if (release != null) {
                    val remoteVersion = release.tagName.removePrefix("v").trim()
                    val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }

                    if (isNewerVersion(remoteVersion, currentVersion) && apkAsset != null) {
                        _status.value = UpdateStatus.UpdateAvailable(
                            latestVersion = remoteVersion,
                            releaseName = release.name ?: "Release $remoteVersion",
                            changelog = release.body ?: "Nessun changelog specificato.",
                            downloadUrl = apkAsset.browserDownloadUrl,
                            apkSize = apkAsset.size
                        )
                    } else {
                        _status.value = UpdateStatus.NoUpdateAvailable(currentVersion)
                    }
                } else {
                    _status.value = UpdateStatus.Error("Risposta vuota da GitHub")
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Repository '$repoOwner/$repoName' o release non trovata. Controlla il nome o inserisci un Token per repository privati."
                    401 -> "Token non valido o permessi insufficienti."
                    403 -> "Rate limit GitHub superato. Inserisci un Personal Access Token."
                    else -> "Errore GitHub: HTTP ${response.code()}"
                }
                _status.value = UpdateStatus.Error(errorMsg)
            }
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("Impossibile verificare gli aggiornamenti: ${e.localizedMessage}")
        }
    }

    suspend fun downloadAndInstall(downloadUrl: String, version: String) = withContext(Dispatchers.IO) {
        val authHeader = if (personalAccessToken.isNotBlank()) "Bearer $personalAccessToken" else null
        val requestBuilder = Request.Builder().url(downloadUrl)
        if (authHeader != null) {
            requestBuilder.addHeader("Authorization", authHeader)
            requestBuilder.addHeader("Accept", "application/octet-stream")
        }

        try {
            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                _status.value = UpdateStatus.Error("Download fallito: HTTP ${response.code}")
                return@withContext
            }

            val body = response.body
            if (body == null) {
                _status.value = UpdateStatus.Error("Corpo della risposta vuoto")
                return@withContext
            }

            val totalBytes = body.contentLength()
            val destinationDir = File(context.cacheDir, "apk_updates")
            if (!destinationDir.exists()) destinationDir.mkdirs()
            val destinationFile = File(destinationDir, "blueandme_connect_v$version.apk")

            body.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var downloaded: Long = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead
                        val progress = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else -1
                        _status.value = UpdateStatus.Downloading(progress, downloaded, totalBytes)
                    }
                    output.flush()
                }
            }

            _status.value = UpdateStatus.ReadyToInstall(destinationFile, version)
            installApk(destinationFile)
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("Errore durante il download dell'APK: ${e.localizedMessage}")
        }
    }

    fun installApk(apkFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("Impossibile avviare il pacchetto di installazione: ${e.localizedMessage}")
        }
    }

    fun resetStatus() {
        _status.value = UpdateStatus.Idle
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
