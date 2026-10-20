package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SpeedometerGauge(
    speedKmH: Float,
    rpm: Int,
    currentGear: Int,
    suggestedGear: Int?,
    modifier: Modifier = Modifier,
    maxSpeed: Float = 220f
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedKmH.coerceIn(0f, maxSpeed),
        animationSpec = tween(durationMillis = 100),
        label = "speed"
    )

    val animatedRpm by animateFloatAsState(
        targetValue = rpm.toFloat().coerceIn(0f, 7000f),
        animationSpec = tween(durationMillis = 100),
        label = "rpm"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("speedometer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        // Disegno archi tachimetro e contagiri
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthSpeed = 14.dp.toPx()
            val strokeWidthRpm = 6.dp.toPx()

            val sweepAngle = 240f
            val startAngle = 150f

            // 1. Traccia di fondo tachimetro
            drawArc(
                color = GaugeTrack,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidthSpeed / 2, strokeWidthSpeed / 2),
                size = Size(size.width - strokeWidthSpeed, size.height - strokeWidthSpeed),
                style = Stroke(width = strokeWidthSpeed, cap = StrokeCap.Round)
            )

            // 2. Arco attivo tachimetro con gradiente Ciano
            val speedFraction = (animatedSpeed / maxSpeed).coerceIn(0f, 1f)
            if (speedFraction > 0.01f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyanAccentDim, CyanAccent, EcoEmerald)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * speedFraction,
                    useCenter = false,
                    topLeft = Offset(strokeWidthSpeed / 2, strokeWidthSpeed / 2),
                    size = Size(size.width - strokeWidthSpeed, size.height - strokeWidthSpeed),
                    style = Stroke(width = strokeWidthSpeed, cap = StrokeCap.Round)
                )
            }

            // 3. Arco RPM interno
            val rpmPadding = 26.dp.toPx()
            val rpmSize = size.width - (rpmPadding * 2)
            val rpmFraction = (animatedRpm / 7000f).coerceIn(0f, 1f)

            // Fondo RPM
            drawArc(
                color = GaugeTrack.copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(rpmPadding, rpmPadding),
                size = Size(rpmSize, rpmSize),
                style = Stroke(width = strokeWidthRpm, cap = StrokeCap.Round)
            )

            // Valore attivo RPM (diventa arancio/rosso sopra i 4500 giri)
            val rpmColor = if (rpm > 5000) DangerRed else if (rpm > 3500) WarningAmber else CyanAccentDim
            if (rpmFraction > 0.01f) {
                drawArc(
                    color = rpmColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * rpmFraction,
                    useCenter = false,
                    topLeft = Offset(rpmPadding, rpmPadding),
                    size = Size(rpmSize, rpmSize),
                    style = Stroke(width = strokeWidthRpm, cap = StrokeCap.Round)
                )
            }
        }

        // Display digitale centrale
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = animatedSpeed.toInt().toString(),
                color = TextPrimary,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                lineHeight = 58.sp
            )
            Text(
                text = "km/h",
                color = CyanAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${animatedRpm.toInt()}",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "RPM",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Badge Gear Shift Indicator (GSI) nell'angolo in basso a destra del gauge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-12).dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(AutomotiveCard)
                .border(2.dp, if (suggestedGear != null) EcoEmerald else AutomotiveCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (suggestedGear != null && suggestedGear > currentGear) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Cambia marcia a salire",
                        tint = EcoEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (suggestedGear != null && suggestedGear < currentGear) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Cambia marcia a scendere",
                        tint = WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = if (suggestedGear != null) "$suggestedGear" else "$currentGear",
                    color = if (suggestedGear != null) EcoEmerald else TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
