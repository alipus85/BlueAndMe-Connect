package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun EcoScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    sizeDp: Int = 120
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "eco_score"
    )

    val ringColor = when {
        score >= 80 -> EcoEmerald
        score >= 60 -> WarningAmber
        else -> DangerRed
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .testTag("eco_score_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val startAngle = 140f
            val sweepAngle = 260f

            // Fondo traccia
            drawArc(
                color = GaugeTrack,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Arco progresso
            val fraction = (animatedScore / 100f).coerceIn(0f, 1f)
            if (fraction > 0.01f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(ringColor.copy(alpha = 0.6f), ringColor)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * fraction,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "eco:Drive",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${animatedScore.toInt()}",
                color = ringColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/100",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
