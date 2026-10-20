package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.TripData
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.*

@Composable
fun TripComputerScreen(
    tripA: TripData,
    tripB: TripData,
    onResetTrip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val currentTrip = if (selectedTab == 0) tripA else tripB

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intestazione con selettore Trip A / Trip B
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Trip Computer Blue&Me",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Statistiche di percorrenza sincronizzate con il quadro di bordo",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Tab switch Trip A / Trip B
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(AutomotiveCard)
                    .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) CyanAccent else AutomotiveCard,
                        contentColor = if (selectedTab == 0) AutomotiveBlack else TextSecondary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Trip A", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) CyanAccent else AutomotiveCard,
                        contentColor = if (selectedTab == 1) AutomotiveBlack else TextSecondary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Trip B", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Griglia parametri di viaggio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TelemetryCard(
                title = "Distanza Percorsa",
                value = String.format("%.1f", currentTrip.distanceKm),
                unit = "km",
                icon = Icons.Default.Route,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Tempo di Guida",
                value = currentTrip.durationFormatted,
                unit = "ore",
                icon = Icons.Default.AccessTime,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TelemetryCard(
                title = "Velocità Media",
                value = String.format("%.1f", currentTrip.avgSpeedKmH),
                unit = "km/h",
                icon = Icons.Default.Speed,
                accentColor = EcoEmerald,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Consumo Medio",
                value = String.format("%.1f", currentTrip.avgConsumptionL100Km),
                unit = "l/100km",
                icon = Icons.Default.LocalGasStation,
                accentColor = EcoEmerald,
                modifier = Modifier.weight(1f)
            )
        }

        // Consumo Istantaneo a barra piena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AutomotiveCard)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consumo Istantaneo",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.1f", currentTrip.instantConsumptionL100Km)} l/100km",
                        color = CyanAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentTrip.instantConsumptionL100Km / 15f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (currentTrip.instantConsumptionL100Km > 9f) WarningAmber else EcoEmerald,
                    trackColor = GaugeTrack
                )
            }
        }

        // Tasto Reset del Trip selezionato
        Button(
            onClick = { onResetTrip(currentTrip.name) },
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerRed.copy(alpha = 0.2f),
                contentColor = DangerRed
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .testTag("reset_trip_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Azzera ${currentTrip.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
