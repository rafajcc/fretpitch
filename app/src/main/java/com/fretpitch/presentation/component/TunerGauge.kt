package com.fretpitch.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.fretpitch.presentation.theme.AccentBlue
import com.fretpitch.presentation.theme.AccentGreen
import com.fretpitch.presentation.theme.AccentRed
import com.fretpitch.presentation.theme.DarkSurfaceVariant
import com.fretpitch.presentation.theme.TextSecondary
import kotlin.math.abs

@Composable
fun TunerGauge(
    centsOffset: Float,
    isInTune: Boolean,
    modifier: Modifier = Modifier
) {
    val clampedCents = centsOffset.coerceIn(-50f, 50f)
    val animatedOffset = remember { Animatable(0f) }

    LaunchedEffect(clampedCents) {
        animatedOffset.animateTo(
            targetValue = clampedCents,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val tuneColor = when {
        isInTune -> AccentGreen
        abs(clampedCents) <= 15f -> Color(0xFFFFB74D)
        else -> AccentRed
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight * 0.85f
        val radius = canvasWidth * 0.38f
        val strokeWidth = 12.dp.toPx()

        val startAngle = 150f
        val sweepAngle = 240f
        val totalSweep = sweepAngle

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    AccentRed,
                    Color(0xFFFFB74D),
                    AccentGreen,
                    Color(0xFFFFB74D),
                    AccentRed
                ),
                center = Offset(centerX, centerY)
            ),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val neutralColor = DarkSurfaceVariant
        drawArc(
            brush = SolidColor(neutralColor),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
        )

        for (cents in -50..50 step 10) {
            val fraction = (cents + 50f) / 100f
            val angle = startAngle + fraction * totalSweep
            val isMajor = cents % 25 == 0
            val innerRadius = if (isMajor) radius - strokeWidth * 1.2f else radius - strokeWidth * 0.8f
            val outerRadius = radius + strokeWidth * 0.5f

            rotate(angle - 180f, pivot = Offset(centerX, centerY)) {
                drawLine(
                    color = if (cents == 0) AccentGreen else TextSecondary.copy(alpha = 0.5f),
                    start = Offset(centerX, centerY + innerRadius),
                    end = Offset(centerX, centerY + outerRadius),
                    strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        val needleFraction = (animatedOffset.value + 50f) / 100f
        val needleAngle = startAngle + needleFraction * totalSweep - 180f

        val needleLength = radius * 0.85f
        val needleBaseWidth = 4.dp.toPx()

        rotate(needleAngle, pivot = Offset(centerX, centerY)) {
            drawLine(
                color = tuneColor,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - needleLength),
                strokeWidth = needleBaseWidth,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = tuneColor,
                radius = needleBaseWidth * 2f,
                center = Offset(centerX, centerY)
            )
        }

        drawCircle(
            color = Color.DarkGray,
            radius = strokeWidth * 0.8f,
            center = Offset(centerX, centerY)
        )
    }
}
