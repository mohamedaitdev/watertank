package com.watertank.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertank.app.ui.theme.AccentGreen
import com.watertank.app.ui.theme.StrokeSubtle
import com.watertank.app.ui.theme.TextMuted
import com.watertank.app.ui.theme.TextPrimary
import com.watertank.app.ui.theme.WaterMid
import com.watertank.app.ui.theme.WaterTop

/**
 * Semi-circular gauge. Shows a numeric value inside a 240° sweep.
 */
@Composable
fun FlowGauge(
    value: Float,               // current value
    maxValue: Float,            // scale
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    accent: Color = AccentGreen
) {
    val fraction = (value / maxValue).coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = fraction, animationSpec = tween(900), label = "gauge"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14f
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Track
            drawArc(
                color = StrokeSubtle,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            // Progress
            drawArc(
                brush = Brush.sweepGradient(
                    0f to WaterMid, 0.6f to accent, 1f to WaterTop
                ),
                startAngle = 150f,
                sweepAngle = 240f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
        }
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatShort(value),
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(text = unit, color = TextMuted, fontSize = 11.sp)
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatShort(v: Float): String {
    val abs = kotlin.math.abs(v)
    return when {
        abs >= 10_000 -> "%.1fk".format(v / 1000)
        abs >= 1_000  -> "%.1fk".format(v / 1000)
        abs >= 100    -> "%.0f".format(v)
        else          -> "%.1f".format(v)
    }
}
