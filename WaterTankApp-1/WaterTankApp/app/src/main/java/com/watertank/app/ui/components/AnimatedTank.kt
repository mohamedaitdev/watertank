package com.watertank.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertank.app.ui.theme.BgCard
import com.watertank.app.ui.theme.StrokeSubtle
import com.watertank.app.ui.theme.TextMuted
import com.watertank.app.ui.theme.WaterDeep
import com.watertank.app.ui.theme.WaterMid
import com.watertank.app.ui.theme.WaterShine
import com.watertank.app.ui.theme.WaterTop
import kotlin.math.sin

/**
 * Animated horizontal cylindrical tank (echoes the real tanks in the facility).
 * - Shows fill % as a rising water body with two overlapping sine waves for motion.
 * - Animates the fill smoothly whenever [fillFraction] changes.
 * - A specular highlight + rim gives it a metallic look.
 */
@Composable
fun AnimatedTank(
    fillFraction: Float,            // 0f..1f
    modifier: Modifier = Modifier,
    showPercentText: Boolean = true
) {
    val target = fillFraction.coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1200),
        label = "fill"
    )

    // Continuous wave phase
    val phase = rememberInfiniteTransition(label = "wave").let { it ->
        val a = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            a.animateTo(
                targetValue = (2 * Math.PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(3800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
        a.value
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val w = maxWidth
        val tankHeight = (w.value * 0.55f).dp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tankHeight)
                    .padding(horizontal = 16.dp)
            ) {
                val pad = size.minDimension * 0.04f
                val tankRect = Size(size.width - pad * 2, size.height - pad * 2)
                val topLeft = Offset(pad, pad)

                val cornerR = tankRect.height / 2f

                // Shell background (dark metal)
                drawRoundedTank(
                    topLeft = topLeft,
                    size = tankRect,
                    corner = cornerR,
                    body = Brush.verticalGradient(
                        0f to BgCard,
                        1f to Color(0xFF0D1622)
                    ),
                    stroke = StrokeSubtle
                )

                // Water fill - clipped to the rounded body
                val path = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(
                                offset = topLeft,
                                size = tankRect
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR, cornerR)
                        )
                    )
                }
                clipPath(path) {
                    val waterHeight = tankRect.height * animatedFill
                    val waterTopY = topLeft.y + tankRect.height - waterHeight

                    // Water body gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to WaterTop,
                            0.5f to WaterMid,
                            1f to WaterDeep,
                            startY = waterTopY,
                            endY = topLeft.y + tankRect.height
                        ),
                        topLeft = Offset(topLeft.x, waterTopY),
                        size = Size(tankRect.width, waterHeight + 2f)
                    )

                    if (animatedFill > 0.01f) {
                        // Two sine waves for surface motion
                        drawWave(
                            baseline = waterTopY,
                            width = tankRect.width,
                            left = topLeft.x,
                            height = size.minDimension * 0.02f,
                            phase = phase,
                            color = WaterShine.copy(alpha = 0.55f)
                        )
                        drawWave(
                            baseline = waterTopY + 4f,
                            width = tankRect.width,
                            left = topLeft.x,
                            height = size.minDimension * 0.015f,
                            phase = phase * 1.3f + 1f,
                            color = WaterShine.copy(alpha = 0.25f)
                        )
                    }
                }

                // Specular highlight top
                drawRoundedTank(
                    topLeft = topLeft,
                    size = tankRect,
                    corner = cornerR,
                    body = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.08f),
                        0.25f to Color.Transparent,
                        1f to Color.Transparent
                    ),
                    stroke = null
                )

                // Bolted rim rings (left and right)
                val rimX1 = topLeft.x + tankRect.width * 0.18f
                val rimX2 = topLeft.x + tankRect.width * 0.82f
                listOf(rimX1, rimX2).forEach { x ->
                    drawLine(
                        color = StrokeSubtle,
                        start = Offset(x, topLeft.y + 6f),
                        end = Offset(x, topLeft.y + tankRect.height - 6f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // Outer stroke
                drawPath(
                    path = path,
                    color = StrokeSubtle,
                    style = Stroke(width = 2f)
                )
            }

            if (showPercentText) {
                val pct = (animatedFill * 100f)
                Text(
                    text = "%.1f%%".format(pct),
                    color = WaterTop,
                    fontWeight = FontWeight.Black,
                    fontSize = 44.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "fill level",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundedTank(
    topLeft: Offset,
    size: Size,
    corner: Float,
    body: Brush,
    stroke: Color?
) {
    drawRoundRect(
        brush = body,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
    )
    stroke?.let {
        drawRoundRect(
            color = it,
            topLeft = topLeft,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = 2f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWave(
    baseline: Float,
    width: Float,
    left: Float,
    height: Float,
    phase: Float,
    color: Color
) {
    val path = Path()
    val step = 6f
    var x = 0f
    path.moveTo(left, baseline + height)
    while (x <= width) {
        val y = baseline + sin((x / width) * 4f * Math.PI.toFloat() + phase) * height
        path.lineTo(left + x, y)
        x += step
    }
    path.lineTo(left + width, baseline + height + 40f)
    path.lineTo(left, baseline + height + 40f)
    path.close()
    drawPath(path, color = color)
}

/** Small inline KPI badge used on cards. */
@Composable
fun InlineKpi(label: String, value: String, accent: Color = WaterMid, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
