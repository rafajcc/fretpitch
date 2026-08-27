package com.fretpitch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fretpitch.data.audio.TonePlayer
import com.fretpitch.data.mapper.FrequencyMapper
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import com.fretpitch.domain.model.TunerState
import com.fretpitch.domain.repository.PitchDetector
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
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

@HiltViewModel
class TunerViewModel @Inject constructor(
    private val pitchDetector: PitchDetector,
    private val frequencyMapper: FrequencyMapper,
    private val tonePlayer: TonePlayer
) : ViewModel() {

    private val _tunerState = MutableStateFlow(TunerState())
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    private var listeningJob: Job? = null
    private var silenceJob: Job? = null
    private var lastTunedString: GuitarString? = null

    companion object {
        private const val A4_FREQUENCY = 440f
        private const val A4_MIDI = 69
        private const val GUITAR_MIN_FREQUENCY = 75f
        private const val GUITAR_MAX_FREQUENCY = 1100f
        private const val MIN_AMPLITUDE = 0.018f
        private const val SILENCE_TIMEOUT_MS = 350L
    }

    fun startListening() {
        if (listeningJob?.isActive == true) return

        lastTunedString = null
        _tunerState.update { it.copy(isListening = true) }
        pitchDetector.start()

        listeningJob = viewModelScope.launch {
            pitchDetector.pitchResults().collect { result ->
                if (result.frequency < GUITAR_MIN_FREQUENCY ||
                    result.frequency > GUITAR_MAX_FREQUENCY ||
                    result.confidence < 0.3f ||
                    result.amplitude < MIN_AMPLITUDE
                ) {
                    return@collect
                }

                val midiNote = frequencyToMidi(result.frequency)
                val note = frequencyMapper.midiNoteToNote(midiNote)
                val exactFreq = midiToFrequency(midiNote)
                val cents = frequencyToCents(result.frequency, exactFreq)
                val matchedString = findMatchingString(midiNote)
                val inTune = abs(cents) <= 5f
                val justTuned = matchedString != null && inTune && matchedString != lastTunedString

                lastTunedString = matchedString?.takeIf { inTune }

                _tunerState.update {
                    it.copy(
                        detectedFrequency = result.frequency,
                        detectedNote = note,
                        centsOffset = cents,
                        matchedString = matchedString,
                        confidence = result.confidence,
                        lastUpdateTimeMs = System.currentTimeMillis(),
                        isStringTuned = justTuned
                    )
                }

                if (justTuned) {
                    viewModelScope.launch { tonePlayer.playStringTuned() }
                }

                resetSilenceTimer()
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        silenceJob?.cancel()
        silenceJob = null
        lastTunedString = null
        pitchDetector.stop()
        _tunerState.update {
            it.copy(
                isListening = false,
                detectedFrequency = 0f,
                detectedNote = null,
                centsOffset = 0f,
                matchedString = null,
                confidence = 0f,
                lastUpdateTimeMs = 0L,
                isStringTuned = false
            )
        }
    }

    private fun resetSilenceTimer() {
        silenceJob?.cancel()
        silenceJob = viewModelScope.launch {
            delay(SILENCE_TIMEOUT_MS)
            if (isActive) {
                _tunerState.update {
                    it.copy(
                        centsOffset = 0f,
                        matchedString = null,
                        isStringTuned = false
                    )
                }
            }
        }
    }

    private fun frequencyToMidi(frequency: Float): Int {
        return (12f * log2(frequency / A4_FREQUENCY) + A4_MIDI).roundToInt()
    }

    private fun midiToFrequency(midi: Int): Float {
        return A4_FREQUENCY * 2f.pow((midi - A4_MIDI) / 12f)
    }

    private fun frequencyToCents(detected: Float, target: Float): Float {
        if (target <= 0f || detected <= 0f) return 0f
        return 1200f * log2(detected / target)
    }

    private fun findMatchingString(midiNote: Int): GuitarString? {
        return GuitarString.all().find { it.openNoteMidi == midiNote }
    }
}
