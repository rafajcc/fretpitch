package com.fretpitch.presentation.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fretpitch.R
import com.fretpitch.presentation.component.FeedbackOverlay
import com.fretpitch.presentation.component.ModeSelector
import com.fretpitch.presentation.component.ModeSelectorSheetContent
import com.fretpitch.presentation.component.NoteDisplay
import com.fretpitch.presentation.component.PermissionHandler
import com.fretpitch.presentation.component.SpeedControl
import com.fretpitch.presentation.model.MainUiState
import com.fretpitch.presentation.util.nameResId
import com.fretpitch.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToTuner: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: String = "es",
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showModeSelector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && uiState.isPlaying) {
                viewModel.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!uiState.hasMicPermission) {
        PermissionHandler(
            hasPermission = uiState.hasMicPermission,
            onPermissionResult = { viewModel.setMicPermission(it) }
        )
        return
    }

    if (uiState.sessionResult != null) {
        StatsScreen(
            sessionResult = uiState.sessionResult!!,
            onDismiss = { viewModel.dismissStats() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToTuner) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tuner),
                            contentDescription = stringResource(R.string.nav_tuner)
                        )
                    }
                    Box {
                        IconButton(onClick = { showLanguageMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_language),
                                contentDescription = stringResource(R.string.language)
                            )
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Español") },
                                onClick = {
                                    onLanguageChange("es")
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    onLanguageChange("en")
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                PracticeSection(
                    uiState = uiState,
                    onModeClick = { showModeSelector = true },
                    onSpeedUp = viewModel::increaseSpeed,
                    onSpeedDown = viewModel::decreaseSpeed,
                    onPlayStop = {
                        if (uiState.isPlaying) viewModel.stop() else viewModel.play()
                    }
                )

                if (uiState.isPlaying) {
                    DetectionInfo(uiState = uiState)
                }

                Spacer(modifier = Modifier.weight(1f))
                
                VersionFooter()
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            FeedbackOverlay(
                feedback = uiState.feedback,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (showModeSelector) {
                ModalBottomSheet(
                    onDismissRequest = { showModeSelector = false },
                    sheetState = sheetState
                ) {
                    ModeSelectorSheetContent(
                        selectedNotes = uiState.mode.selectedNotes,
                        selectedStrings = uiState.mode.selectedStrings,
                        includeSharps = uiState.includeSharps,
                        onNoteToggle = viewModel::toggleNote,
                        onStringToggle = viewModel::toggleString,
                        onSelectAllNotes = viewModel::selectAllNotes,
                        onSelectAllStrings = viewModel::selectAllStrings,
                        onSharpsToggle = viewModel::setIncludeSharps
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeSection(
    uiState: MainUiState,
    onModeClick: () -> Unit,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
    onPlayStop: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!uiState.isPlaying) {
            ModeSelector(
                selectedNotes = uiState.mode.selectedNotes,
                selectedStrings = uiState.mode.selectedStrings,
                onClick = onModeClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        NoteDisplay(
            exercise = uiState.currentExercise,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        SpeedControl(
            speedLevel = uiState.speedLevel,
            onSpeedUp = onSpeedUp,
            onSpeedDown = onSpeedDown
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPlayStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isPlaying) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (uiState.isPlaying) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
            )
        ) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (uiState.isPlaying) stringResource(R.string.stop) else stringResource(R.string.play),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.isPlaying && uiState.attempts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "${uiState.attempts.count { it.correct }} / ${uiState.attempts.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetectionInfo(uiState: MainUiState) {
    Spacer(modifier = Modifier.height(32.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.detected_note_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val detectedNote = uiState.detectedNote
        val noteText = detectedNote?.let { stringResource(it.nameResId()) } ?: "—"
        val stringText = uiState.detectedString?.let {
            stringResource(R.string.string_format, it.number)
        }
        
        val isCorrect = detectedNote != null && detectedNote == uiState.currentExercise?.note
        
        Text(
            text = if (stringText != null) "$noteText · $stringText" else noteText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            }
        )
    }
}

@Composable
private fun VersionFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.version_label, com.fretpitch.BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
