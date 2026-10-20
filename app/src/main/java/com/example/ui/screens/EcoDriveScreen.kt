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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EcoDriveState
import com.example.ui.components.EcoScoreGauge
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.*

@Composable
fun EcoDriveScreen(
    ecoState: EcoDriveState,
    suggestedGear: Int?,
    currentGear: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "eco:Drive™ Info",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Analisi dello stile di guida, ottimizzazione consumi ed emissioni CO2",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Sezione centrale: Grande indicatore EcoIndex e consiglio live
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AutomotiveCard)
                    .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EcoScoreGauge(
                    score = ecoState.ecoIndex,
                    sizeDp = 150
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scheda consiglio in tempo reale
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EcoEmerald.copy(alpha = 0.15f))
                        .border(1.dp, EcoEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = EcoEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Consiglio eco:Drive",
                                color = EcoEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ecoState.realTimeAdvice,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Gear Shift Indicator box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AutomotiveCard)
                        .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gear Shift Indicator (GSI)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (suggestedGear != null) "Consigliata marcia $suggestedGear" else "Marcia $currentGear ottimale",
                                color = if (suggestedGear != null) EcoEmerald else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = if (suggestedGear != null && suggestedGear > currentGear) Icons.Default.ArrowUpward
                            else if (suggestedGear != null) Icons.Default.ArrowDownward
                            else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (suggestedGear != null) EcoEmerald else CyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Griglia parametri di dettaglio
        Text(
            text = "Metriche di Guida",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TelemetryCard(
                title = "Accelerazione",
                value = "${ecoState.accelerationScore}",
                unit = "/100",
                icon = Icons.Default.Speed,
                accentColor = EcoEmerald,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Decelerazione",
                value = "${ecoState.decelerationScore}",
                unit = "/100",
                icon = Icons.Default.ElectricCar,
                accentColor = EcoEmerald,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TelemetryCard(
                title = "Uso del Cambio",
                value = "${ecoState.gearShiftScore}",
                unit = "/100",
                icon = Icons.Default.SwapCalls,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Costanza Velocità",
                value = "${ecoState.speedSteadinessScore}",
                unit = "/100",
                icon = Icons.Default.Timeline,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TelemetryCard(
                title = "Emissioni CO2 Stimate",
                value = "${ecoState.co2GramsPerKm}",
                unit = "g/km",
                icon = Icons.Default.Co2,
                accentColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Carburante Risparmiato",
                value = String.format("%.1f", ecoState.fuelSavedLiters),
                unit = "litri",
                icon = Icons.Default.Savings,
                accentColor = EcoEmerald,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
