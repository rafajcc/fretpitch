package com.fretpitch.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fretpitch.presentation.theme.AccentAmber
import com.fretpitch.presentation.theme.AccentGreen
import com.fretpitch.presentation.theme.AccentRed
import com.fretpitch.presentation.theme.DarkSurfaceVariant
import kotlin.math.abs
import kotlin.math.roundToInt

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
            animationSpec = tween(durationMillis = 80)
        )
    }

    val ledCount = 21
    val centerIndex = ledCount / 2
    val stepCents = 5f
    val targetLed = centerIndex + (animatedOffset.value / stepCents)
        .roundToInt()
        .coerceIn(-centerIndex, centerIndex)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val ledWidth = size.width / ledCount
        val gap = ledWidth * 0.22f
        val ledWidthActual = ledWidth - gap
        val ledHeight = size.height * 0.6f
        val topLeftY = (size.height - ledHeight) / 2f
        val cornerRadius = CornerRadius(ledWidthActual * 0.3f, ledWidthActual * 0.3f)

        for (i in 0 until ledCount) {
            val distance = abs(i - centerIndex)
            val x = i * ledWidth + gap / 2f

            val isLit: Boolean
            val ledColor: Color

            if (isInTune) {
                isLit = distance <= 1
                ledColor = AccentGreen
            } else {
                isLit = when {
                    targetLed > centerIndex -> i in centerIndex..targetLed
                    targetLed < centerIndex -> i in targetLed..centerIndex
                    else -> false
                }
                ledColor = when {
                    distance == 0 -> AccentGreen
                    distance <= 2 -> AccentAmber
                    else -> AccentRed
                }
            }

            drawRoundRect(
                color = if (isLit) ledColor else DarkSurfaceVariant,
                topLeft = Offset(x, topLeftY),
                size = Size(ledWidthActual, ledHeight),
                cornerRadius = cornerRadius
            )
        }

        drawCircle(
            color = if (isInTune) AccentGreen else Color.Gray,
            radius = ledWidthActual * 0.5f,
            center = Offset(size.width / 2f, size.height - ledWidthActual * 0.6f)
        )
    }
}