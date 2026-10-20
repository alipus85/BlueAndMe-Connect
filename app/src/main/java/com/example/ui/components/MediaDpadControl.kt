package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Controller D-pad iconico del sistema Blue&Me per saltare brani e navigare tra le cartelle musicali.
 */
@Composable
fun MediaDpadControl(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onNextFolder: () -> Unit,
    onPreviousFolder: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 130
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(AutomotiveCard)
            .border(2.dp, AutomotiveCardBorder, CircleShape)
            .testTag("blueandme_dpad"),
        contentAlignment = Alignment.Center
    ) {
        // Freccia Su: Cartella Successiva
        IconButton(
            onClick = onNextFolder,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(38.dp)
                .testTag("dpad_folder_up")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Cartella Successiva",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Freccia Giù: Cartella Precedente
        IconButton(
            onClick = onPreviousFolder,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(38.dp)
                .testTag("dpad_folder_down")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Cartella Precedente",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Freccia Sinistra: Brano Precedente
        IconButton(
            onClick = onPreviousTrack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(38.dp)
                .testTag("dpad_track_prev")
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Brano Precedente",
                tint = CyanAccent,
                modifier = Modifier.size(22.dp)
            )
        }

        // Freccia Destra: Brano Successivo
        IconButton(
            onClick = onNextTrack,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(38.dp)
                .testTag("dpad_track_next")
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Brano Successivo",
                tint = CyanAccent,
                modifier = Modifier.size(22.dp)
            )
        }

        // Tasto Centrale: Play / Pause
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(CyanAccent)
                .clickable { onPlayPause() }
                .testTag("dpad_play_pause"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausa" else "Riproduci",
                tint = AutomotiveBlack,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
