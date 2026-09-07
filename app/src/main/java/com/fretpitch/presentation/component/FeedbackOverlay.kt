package com.fretpitch.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
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
        val visible = feedback is FeedbackState.Correct || feedback is FeedbackState.Incorrect
        
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            val popScale = remember { Animatable(0.8f) }

            LaunchedEffect(feedback) {
                if (visible) {
                    popScale.snapTo(0.8f)
                    popScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = popScale.value
                        scaleY = popScale.value
                    },
                shape = MaterialTheme.shapes.extraLarge,
                color = when (feedback) {
                    is FeedbackState.Correct -> MaterialTheme.colorScheme.tertiaryContainer
                    is FeedbackState.Incorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                tonalElevation = 6.dp
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
                    modifier = Modifier.padding(40.dp).size(64.dp),
                    tint = when (feedback) {
                        is FeedbackState.Correct -> MaterialTheme.colorScheme.onTertiaryContainer
                        is FeedbackState.Incorrect -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
