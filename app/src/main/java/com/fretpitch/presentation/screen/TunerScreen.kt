package com.fretpitch.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fretpitch.R
import com.fretpitch.presentation.component.TunedStringsBar
import com.fretpitch.presentation.component.TunerGauge
import com.fretpitch.presentation.theme.AccentGreen
import com.fretpitch.presentation.theme.AccentRed
import com.fretpitch.presentation.theme.DarkSurfaceVariant
import com.fretpitch.presentation.theme.TextSecondary
import com.fretpitch.presentation.viewmodel.TunerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    onBack: () -> Unit,
    viewModel: TunerViewModel = hiltViewModel()
) {
    val tunerState by viewModel.tunerState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tuner_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopListening()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TunedStringsBar(
                        tunedStrings = tunerState.tunedStrings,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (tunerState.isListening && tunerState.detectedNote != null) {
                        Text(
                            text = tunerState.noteNameDisplay,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            color = when {
                                tunerState.isInTune -> AccentGreen
                                tunerState.isCloseToTune -> MaterialTheme.colorScheme.secondary
                                else -> AccentRed
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (tunerState.matchedString != null) {
                            Text(
                                text = stringResource(R.string.tuner_string_label, tunerState.matchedString!!.number),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TunerGauge(
                            centsOffset = tunerState.centsOffset,
                            isInTune = tunerState.isInTune,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = tunerState.frequencyDisplay,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tunerState.centsDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                tunerState.isInTune -> AccentGreen
                                tunerState.isCloseToTune -> MaterialTheme.colorScheme.secondary
                                else -> AccentRed
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = when {
                                tunerState.isInTune && tunerState.matchedString != null ->
                                    stringResource(
                                        R.string.tuner_string_tuned,
                                        tunerState.matchedString!!.number
                                    )
                                tunerState.isInTune -> stringResource(R.string.tuner_in_tune)
                                tunerState.centsOffset < 0 -> stringResource(R.string.tuner_too_low)
                                else -> stringResource(R.string.tuner_too_high)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                tunerState.isInTune -> AccentGreen
                                tunerState.isCloseToTune -> MaterialTheme.colorScheme.secondary
                                else -> AccentRed
                            }
                        )
                    } else if (tunerState.isListening) {
                        Spacer(modifier = Modifier.height(60.dp))

                        Icon(
                            painter = painterResource(R.drawable.ic_mic),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.tuner_no_signal),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextSecondary.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "82 Hz - 1000 Hz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary.copy(alpha = 0.3f)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(60.dp))

                        Icon(
                            painter = painterResource(R.drawable.ic_mic_off),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.tuner_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextSecondary.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "E \u2022 A \u2022 D \u2022 G \u2022 B \u2022 E",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        if (tunerState.isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tunerState.isListening) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Icon(
                        painter = if (tunerState.isListening) {
                            painterResource(R.drawable.ic_mic_off)
                        } else {
                            painterResource(R.drawable.ic_mic)
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tunerState.isListening) {
                            stringResource(R.string.tuner_stop)
                        } else {
                            stringResource(R.string.tuner_start)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            AnimatedVisibility(
                visible = tunerState.isStringTuned,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = AccentGreen.copy(alpha = 0.9f),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tunerState.matchedString?.let {
                                stringResource(R.string.tuner_string_label, it.number)
                            } ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
