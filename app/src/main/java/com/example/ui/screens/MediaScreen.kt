package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaTrack
import com.example.ui.components.MediaDpadControl
import com.example.ui.theme.*

@Composable
fun MediaScreen(
    mediaTrack: MediaTrack,
    onPlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLibraryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intestazione con pulsanti Libreria USB e Impostazioni USB (come da manuale Blue&Me)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lettore Multimediale Blue&Me",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sorgente: Ingresso USB di bordo",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showLibraryDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AutomotiveCard,
                        contentColor = CyanAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("usb_library_button")
                ) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Libreria USB", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AutomotiveCard)
                        .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Impostazioni USB", tint = TextSecondary)
                }
            }
        }

        // Layout principale del player: D-pad a sinistra, Informazioni traccia a destra
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AutomotiveCard)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Controller D-pad Blue&Me (Frecce Sinistra/Destra per traccia, Su/Giù per cartella)
            MediaDpadControl(
                isPlaying = mediaTrack.isPlaying,
                onPlayPause = onPlayPause,
                onNextTrack = onNextTrack,
                onPreviousTrack = onPrevTrack,
                onNextFolder = onNextTrack,
                onPreviousFolder = onPrevTrack,
                sizeDp = 150
            )

            // Dettagli brano in riproduzione
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = mediaTrack.folderName,
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = mediaTrack.title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = mediaTrack.artist,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = "Album: ${mediaTrack.album}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Barra di avanzamento e minutaggio
                LinearProgressIndicator(
                    progress = { mediaTrack.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanAccent,
                    trackColor = GaugeTrack
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mediaTrack.positionFormatted,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = mediaTrack.durationFormatted,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Tasti Shuffle e Repeat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = mediaTrack.isShuffle,
                        onClick = onToggleShuffle,
                        label = { Text("Casuale") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = mediaTrack.isRepeat,
                        onClick = onToggleRepeat,
                        label = { Text("Ripeti") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Guida ai comandi sul volante e D-pad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AutomotiveSurface)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Mappatura Comandi Blue&Me",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Frecce sinistra / destra: cambio traccia musicale\n• Frecce su / giù: cambio cartella sulla chiavetta USB\n• Tasto SRC / OK sul volante: Play / Pausa",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }

    // Dialogo Libreria USB (come da Capitolo 8, pag. 15 del manuale)
    if (showLibraryDialog) {
        AlertDialog(
            onDismissRequest = { showLibraryDialog = false },
            title = { Text("Libreria USB Blue&Me", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val categories = listOf(
                        "Qualsiasi (Riproduci tutto casuale)",
                        "Cartelle",
                        "Artisti",
                        "Generi",
                        "Album",
                        "Playlist"
                    )
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AutomotiveCard)
                                .clickable {
                                    onNextTrack()
                                    showLibraryDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Text(category, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLibraryDialog = false }) {
                    Text("Chiudi", color = CyanAccent)
                }
            },
            containerColor = AutomotiveSurface
        )
    }

    // Dialogo Impostazioni USB
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Impostazioni USB Blue&Me", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configurazioni per la riproduzione da chiavetta:", color = TextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Riproduzione automatica all'accensione", color = TextPrimary, fontSize = 13.sp)
                        Switch(checked = mediaTrack.autoplayOnStart, onCheckedChange = {})
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Riproduzione casuale (Shuffle)", color = TextPrimary, fontSize = 13.sp)
                        Switch(checked = mediaTrack.isShuffle, onCheckedChange = { onToggleShuffle() })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ripetizione brano corrente", color = TextPrimary, fontSize = 13.sp)
                        Switch(checked = mediaTrack.isRepeat, onCheckedChange = { onToggleRepeat() })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Fatto", color = CyanAccent)
                }
            },
            containerColor = AutomotiveSurface
        )
    }
}
