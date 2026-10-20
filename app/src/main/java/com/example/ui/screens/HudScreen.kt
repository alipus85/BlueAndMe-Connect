package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothConnectionState
import com.example.data.VehicleBrand
import com.example.model.MediaTrack
import com.example.model.SteeringWheelButton
import com.example.model.VehicleTelemetry
import com.example.ui.components.EcoScoreGauge
import com.example.ui.components.SpeedometerGauge
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.*

@Composable
fun HudScreen(
    telemetry: VehicleTelemetry,
    mediaTrack: MediaTrack,
    ecoScore: Int,
    connectionState: BluetoothConnectionState,
    brand: VehicleBrand,
    onPlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onSimulateSteeringButton: (SteeringWheelButton) -> Unit,
    onNavigateToMedia: () -> Unit,
    onNavigateToEco: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Riga superiore: Brand e stato Blue&Me
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyanAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Veicolo",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = brand.brandName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = brand.tagline,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Badge connessione Blue&Me
            val isConnected = connectionState is BluetoothConnectionState.Connected
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isConnected) EcoEmerald.copy(alpha = 0.15f) else CyanAccentDim.copy(alpha = 0.15f))
                    .border(1.dp, if (isConnected) EcoEmerald else CyanAccentDim, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth Status",
                    tint = if (isConnected) EcoEmerald else CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isConnected) "Blue&Me Connesso" else "Blue&Me Attivo",
                    color = if (isConnected) EcoEmerald else CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Sezione centrale: Tachimetro e Schede Telemetriche
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tachimetro a quadrante
            SpeedometerGauge(
                speedKmH = telemetry.speedKmH,
                rpm = telemetry.rpm,
                currentGear = telemetry.currentGear,
                suggestedGear = telemetry.suggestedGear,
                modifier = Modifier.weight(1f)
            )

            // Schede telemetriche e EcoScore
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryCard(
                        title = "Refrigerante",
                        value = "${telemetry.coolantTempC}",
                        unit = "°C",
                        icon = Icons.Default.DeviceThermostat,
                        accentColor = if (telemetry.coolantTempC > 105) DangerRed else CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "Carburante",
                        value = "${telemetry.fuelPercent}%",
                        subtitle = "Autonomia: ${telemetry.fuelRangeKm} km",
                        icon = Icons.Default.LocalGasStation,
                        accentColor = if (telemetry.fuelPercent <= 15) DangerRed else WarningAmber,
                        isWarning = telemetry.fuelPercent <= 15,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelemetryCard(
                        title = "Batteria",
                        value = String.format("%.1f", telemetry.batteryVoltage),
                        unit = "V",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = EcoEmerald,
                        modifier = Modifier.weight(1f)
                    )

                    // Widget eco:Drive compatto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AutomotiveCard)
                            .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                            .clickable { onNavigateToEco() }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EcoScoreGauge(
                            score = ecoScore,
                            sizeDp = 88
                        )
                    }
                }
            }
        }

        // Widget Mini Media Player Blue&Me USB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AutomotiveCard)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(18.dp))
                .clickable { onNavigateToMedia() }
                .padding(14.dp)
                .testTag("hud_media_player_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyanAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Usb,
                        contentDescription = "USB Media",
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Blue&Me USB Player",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• ${mediaTrack.folderName}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = mediaTrack.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = mediaTrack.artist,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { mediaTrack.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = CyanAccent,
                        trackColor = GaugeTrack
                    )
                }

                // Controlli veloci del media player
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevTrack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Precedente",
                            tint = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mediaTrack.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = AutomotiveBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = onNextTrack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Successivo",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }

        // Barra Rapida Simulazione Comandi al Volante (iHidReport 0x4D)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AutomotiveSurface)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Comandi al Volante Fiat/Blue&Me (iHidReport 0x4D)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { onSimulateSteeringButton(SteeringWheelButton.ESC_BACK) },
                    label = { Text("ESC") },
                    leadingIcon = { Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = { onSimulateSteeringButton(SteeringWheelButton.ARROW_UP) },
                    label = { Text("▲") }
                )
                AssistChip(
                    onClick = { onSimulateSteeringButton(SteeringWheelButton.OK_ENTER) },
                    label = { Text("OK") }
                )
                AssistChip(
                    onClick = { onSimulateSteeringButton(SteeringWheelButton.ARROW_DOWN) },
                    label = { Text("▼") }
                )
                AssistChip(
                    onClick = { onSimulateSteeringButton(SteeringWheelButton.MENU_SOURCE) },
                    label = { Text("SRC/MENU") }
                )
                AssistChip(
                    onClick = { onSimulateSteeringButton(SteeringWheelButton.PHONE) },
                    label = { Text("TEL") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}
