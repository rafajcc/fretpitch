package com.fretpitch.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fretpitch.R
import com.fretpitch.domain.model.Exercise
import com.fretpitch.presentation.util.nameResId

@Composable
fun NoteDisplay(
    exercise: Exercise?,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val width = this.maxWidth
        // Adaptative font sizes based on container width
        val noteFontSize = (width.value * 0.3f).coerceIn(80f, 220f).sp
        val labelFontSize = (width.value * 0.08f).coerceIn(24f, 56f).sp

        AnimatedContent(
            targetState = exercise,
            transitionSpec = {
                (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = tween(400))) togetherWith
                (scaleOut(targetScale = 0.8f) + fadeOut(animationSpec = tween(300)))
            },
            label = "note_transition"
        ) { currentExercise ->
            if (currentExercise != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(currentExercise.note.nameResId()),
                        fontSize = noteFontSize,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Light,
                        lineHeight = noteFontSize
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.string_format, currentExercise.guitarString.number),
                        fontSize = labelFontSize,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.press_play),
                    fontSize = labelFontSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
