package com.fretpitch.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.presentation.theme.AccentGreen
import com.fretpitch.presentation.theme.DarkSurfaceVariant
import com.fretpitch.presentation.theme.TextSecondary

@Composable
fun TunedStringsBar(
    tunedStrings: Set<GuitarString>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GuitarString.all().sortedByDescending { it.number }.forEach { string ->
            val tuned = string in tunedStrings
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (tuned) AccentGreen else DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = string.noteName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (tuned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            TextSecondary
                        }
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = string.number.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tuned) AccentGreen else TextSecondary.copy(alpha = 0.5f)
                )
            }
        }
    }
}