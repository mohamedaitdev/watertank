package com.watertank.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.watertank.app.ui.theme.GasYellow
import com.watertank.app.ui.theme.PipeBlue
import com.watertank.app.ui.theme.PipeDark
import com.watertank.app.ui.theme.StrokeSubtle
import com.watertank.app.ui.theme.WaterTop

/**
 * A stylised schematic: chlorine injection → tank → pumps → outlet.
 * Animated dashed flow shows direction of water.
 */
@Composable
fun PipeNetwork(modifier: Modifier = Modifier) {
    val trans = rememberInfiniteTransition(label = "pipes")
    val dashPhase by trans.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height
        val pipeStroke = 10f

        // Chlorine inlet (yellow) on left
        drawLine(
            color = GasYellow,
            start = Offset(w * 0.05f, h * 0.35f),
            end = Offset(w * 0.18f, h * 0.35f),
            strokeWidth = pipeStroke,
            cap = StrokeCap.Round
        )
        // Chlorine down
        drawLine(
            color = GasYellow,
            start = Offset(w * 0.18f, h * 0.35f),
            end = Offset(w * 0.18f, h * 0.65f),
            strokeWidth = pipeStroke,
            cap = StrokeCap.Round
        )

        // Tank block (rounded rectangle)
        drawRoundRect(
            color = PipeDark,
            topLeft = Offset(w * 0.22f, h * 0.3f),
            size = Size(w * 0.35f, h * 0.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
        )
        drawRoundRect(
            color = StrokeSubtle,
            topLeft = Offset(w * 0.22f, h * 0.3f),
            size = Size(w * 0.35f, h * 0.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f),
            style = Stroke(width = 2f)
        )
        // Water level inside tank (static 60%)
        drawRect(
            color = WaterTop.copy(alpha = 0.6f),
            topLeft = Offset(w * 0.23f, h * 0.58f),
            size = Size(w * 0.33f, h * 0.21f)
        )

        // Outlet pipe to pumps
        drawPipeDashed(
            start = Offset(w * 0.57f, h * 0.55f),
            end = Offset(w * 0.78f, h * 0.55f),
            color = PipeBlue,
            phase = dashPhase,
            strokeWidth = pipeStroke
        )

        // Pump block
        drawRoundRect(
            color = PipeBlue,
            topLeft = Offset(w * 0.78f, h * 0.42f),
            size = Size(w * 0.14f, h * 0.3f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        // Pump shaft (small circle)
        drawCircle(
            color = Color(0xFF5FA8FF),
            radius = h * 0.06f,
            center = Offset(w * 0.85f, h * 0.57f)
        )

        // Outlet up
        drawPipeDashed(
            start = Offset(w * 0.85f, h * 0.42f),
            end = Offset(w * 0.85f, h * 0.12f),
            color = PipeBlue,
            phase = dashPhase,
            strokeWidth = pipeStroke
        )
        drawPipeDashed(
            start = Offset(w * 0.85f, h * 0.12f),
            end = Offset(w * 0.95f, h * 0.12f),
            color = PipeBlue,
            phase = dashPhase,
            strokeWidth = pipeStroke
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPipeDashed(
    start: Offset,
    end: Offset,
    color: Color,
    phase: Float,
    strokeWidth: Float
) {
    drawLine(
        color = color.copy(alpha = 0.45f),
        start = start, end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = 0.6f),
        start = start, end = end,
        strokeWidth = strokeWidth * 0.45f,
        cap = StrokeCap.Butt,
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
            floatArrayOf(14f, 14f), phase
        )
    )
}
