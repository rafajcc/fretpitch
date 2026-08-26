package com.fretpitch.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fretpitch.R
import com.fretpitch.presentation.model.FeedbackState

@Composable
fun FeedbackOverlay(
    feedback: FeedbackState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = feedback is FeedbackState.Correct || feedback is FeedbackState.Incorrect,
            enter = scaleIn(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(200)),
            exit = scaleOut(
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            val scale = remember { Animatable(0f) }

            LaunchedEffect(feedback) {
                if (feedback is FeedbackState.Correct || feedback is FeedbackState.Incorrect) {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }

            Surface(
                modifier = Modifier.scale(scale.value),
                shape = MaterialTheme.shapes.extraLarge,
                color = when (feedback) {
                    is FeedbackState.Correct -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                    is FeedbackState.Incorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                    else -> MaterialTheme.colorScheme.surface
                },
                tonalElevation = 8.dp
            ) {
                Icon(
                    imageVector = when (feedback) {
                        is FeedbackState.Correct -> Icons.Default.Check
                        is FeedbackState.Incorrect -> Icons.Default.Close
                        else -> Icons.Default.Check
                    },
                    contentDescription = when (feedback) {
                        is FeedbackState.Correct -> stringResource(R.string.feedback_correct)
                        is FeedbackState.Incorrect -> stringResource(R.string.feedback_incorrect)
                        else -> ""
                    },
                    modifier = Modifier.padding(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
