package com.fretpitch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fretpitch.data.audio.TonePlayer
import com.fretpitch.data.mapper.FrequencyMapper
import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.Exercise
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import com.fretpitch.domain.repository.PitchDetector
import com.fretpitch.domain.usecase.CalculateStatsUseCase
import com.fretpitch.domain.usecase.ExerciseAttempt
import com.fretpitch.domain.usecase.GenerateExerciseUseCase
import com.fretpitch.presentation.model.FeedbackState
import com.fretpitch.presentation.model.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val generateExerciseUseCase: GenerateExerciseUseCase,
    private val calculateStatsUseCase: CalculateStatsUseCase,
    private val pitchDetector: PitchDetector,
    private val frequencyMapper: FrequencyMapper,
    private val tonePlayer: TonePlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var exerciseJob: Job? = null
    private var pitchCollectionJob: Job? = null

    companion object {
        private const val MIN_AMPLITUDE = 0.02f
        private const val POLL_INTERVAL_MS = 50L
        private const val FEEDBACK_DISPLAY_MS = 500L
    }

    fun setMode(mode: AppMode) {
        if (_uiState.value.isPlaying) return
        _uiState.update { it.copy(mode = mode) }
    }

    fun setIncludeSharps(include: Boolean) {
        if (_uiState.value.isPlaying) return
        _uiState.update { it.copy(includeSharps = include) }
    }

    fun increaseSpeed() {
        _uiState.update {
            it.copy(speedLevel = (it.speedLevel + 1).coerceAtMost(10))
        }
    }

    fun decreaseSpeed() {
        _uiState.update {
            it.copy(speedLevel = (it.speedLevel - 1).coerceAtLeast(1))
        }
    }

    fun setMicPermission(granted: Boolean) {
        _uiState.update { it.copy(hasMicPermission = granted) }
    }

    fun play() {
        if (_uiState.value.isPlaying) return
        if (!_uiState.value.hasMicPermission) return

        _uiState.update {
            it.copy(
                isPlaying = true,
                attempts = emptyList(),
                sessionResult = null,
                startTimeMs = System.currentTimeMillis()
            )
        }

        pitchDetector.start()
        startPitchCollection()
        startExerciseLoop()
    }

    fun stop() {
        exerciseJob?.cancel()
        pitchCollectionJob?.cancel()

        pitchDetector.stop()

        val state = _uiState.value
        val timeElapsed = System.currentTimeMillis() - state.startTimeMs

        val result = calculateStatsUseCase(state.attempts, timeElapsed)

        _uiState.update {
            it.copy(
                isPlaying = false,
                currentExercise = null,
                feedback = FeedbackState.None,
                sessionResult = result
            )
        }
    }

    fun dismissStats() {
        _uiState.update { it.copy(sessionResult = null) }
    }

    private fun startExerciseLoop() {
        exerciseJob = viewModelScope.launch {
            while (isActive) {
                val exercise = generateExerciseUseCase(
                    _uiState.value.mode,
                    _uiState.value.includeSharps
                )

                _uiState.update {
                    it.copy(
                        currentExercise = exercise,
                        feedback = FeedbackState.Listening
                    )
                }

                val deadline = System.currentTimeMillis() + _uiState.value.intervalMs
                while (isActive && System.currentTimeMillis() < deadline) {
                    if (_uiState.value.feedback != FeedbackState.Listening) break
                    delay(POLL_INTERVAL_MS)
                }

                if (_uiState.value.feedback == FeedbackState.Listening) {
                    handleResult(false)
                }

                delay(FEEDBACK_DISPLAY_MS)
            }
        }
    }

    private fun startPitchCollection() {
        pitchCollectionJob = viewModelScope.launch {
            pitchDetector.pitchResults().collect { result ->
                if (_uiState.value.feedback != FeedbackState.Listening) return@collect
                if (result.amplitude < MIN_AMPLITUDE) return@collect

                val exercise = _uiState.value.currentExercise ?: return@collect

                if (frequencyMapper.isNoteCorrect(result.frequency, exercise.expectedFrequency)) {
                    handleResult(true)
                }
            }
        }
    }

    private fun handleResult(correct: Boolean) {
        val exercise = _uiState.value.currentExercise ?: return

        _uiState.update {
            it.copy(
                feedback = if (correct) FeedbackState.Correct else FeedbackState.Incorrect,
                attempts = it.attempts + ExerciseAttempt(exercise, correct)
            )
        }

        if (correct) {
            viewModelScope.launch { tonePlayer.playCorrect() }
        } else {
            viewModelScope.launch { tonePlayer.playIncorrect() }
        }
    }
}
