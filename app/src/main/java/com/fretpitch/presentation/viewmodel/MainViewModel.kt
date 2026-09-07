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
    private var silenceTimerJob: Job? = null

    private var wrongNoteStartMs: Long = 0L
    private var lastWrongMidi: Int? = null
    private var lastExerciseNote: Note? = null
    private var lastExerciseString: GuitarString? = null

    companion object {
        private const val MIN_AMPLITUDE = 0.008f
        private const val PLAYED_NOTE_AMPLITUDE = 0.015f
        private const val PLAYED_NOTE_CONFIDENCE = 0.15f
        private const val WRONG_NOTE_SUSTAIN_MS = 250L
        private const val POLL_INTERVAL_MS = 50L
        private const val FEEDBACK_DISPLAY_MS = 500L
        private const val DETECTED_NOTE_HOLD_MS = 1500L
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

        lastExerciseNote = null
        lastExerciseString = null
        wrongNoteStartMs = 0L
        lastWrongMidi = null

        _uiState.update {
            it.copy(
                isPlaying = true,
                attempts = emptyList(),
                sessionResult = null,
                detectedNote = null,
                detectedString = null,
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
        silenceTimerJob?.cancel()

        pitchDetector.stop()

        val state = _uiState.value
        val timeElapsed = System.currentTimeMillis() - state.startTimeMs

        val result = calculateStatsUseCase(state.attempts, timeElapsed)

        _uiState.update {
            it.copy(
                isPlaying = false,
                currentExercise = null,
                feedback = FeedbackState.None,
                sessionResult = result,
                detectedNote = null,
                detectedString = null
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
                    _uiState.value.includeSharps,
                    lastExerciseNote,
                    lastExerciseString
                )

                lastExerciseNote = exercise.note
                lastExerciseString = exercise.guitarString

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
                if (result.amplitude < MIN_AMPLITUDE) return@collect

                val midi = frequencyMapper.frequencyToMidiNote(result.frequency)
                val detected = midi?.let { frequencyMapper.midiNoteToNote(it) }
                val detectedGuitarString = midi?.let { midiNote ->
                    GuitarString.all().find { it.openNoteMidi == midiNote }
                }
                _uiState.update {
                    it.copy(
                        detectedNote = detected,
                        detectedString = detectedGuitarString
                    )
                }
                resetDetectedNoteTimer()

                if (_uiState.value.feedback != FeedbackState.Listening) return@collect

                val exercise = _uiState.value.currentExercise ?: return@collect

                if (frequencyMapper.isNoteCorrect(result.frequency, exercise.expectedFrequency)) {
                    wrongNoteStartMs = 0L
                    lastWrongMidi = null
                    handleResult(true)
                    return@collect
                }

                val wrongMidi = frequencyMapper.frequencyToMidiNote(result.frequency)
                if (wrongMidi == null) return@collect

                if (result.amplitude >= PLAYED_NOTE_AMPLITUDE &&
                    result.confidence <= PLAYED_NOTE_CONFIDENCE
                ) {
                    val now = System.currentTimeMillis()
                    if (wrongMidi == lastWrongMidi && wrongNoteStartMs > 0L) {
                        if (now - wrongNoteStartMs >= WRONG_NOTE_SUSTAIN_MS) {
                            wrongNoteStartMs = 0L
                            lastWrongMidi = null
                            handleResult(false)
                        }
                    } else {
                        wrongNoteStartMs = now
                        lastWrongMidi = wrongMidi
                    }
                } else {
                    wrongNoteStartMs = 0L
                    lastWrongMidi = wrongMidi
                }
            }
        }
    }

    private fun resetDetectedNoteTimer() {
        silenceTimerJob?.cancel()
        silenceTimerJob = viewModelScope.launch {
            delay(DETECTED_NOTE_HOLD_MS)
            if (isActive) {
                _uiState.update {
                    it.copy(
                        detectedNote = null,
                        detectedString = null
                    )
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
