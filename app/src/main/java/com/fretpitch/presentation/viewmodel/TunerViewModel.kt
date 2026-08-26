package com.fretpitch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fretpitch.data.mapper.FrequencyMapper
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import com.fretpitch.domain.model.TunerState
import com.fretpitch.domain.repository.PitchDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val frequencyMapper: FrequencyMapper
) : ViewModel() {

    private val _tunerState = MutableStateFlow(TunerState())
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    private var listeningJob: Job? = null

    companion object {
        private const val A4_FREQUENCY = 440f
        private const val A4_MIDI = 69
        private const val GUITAR_MIN_FREQUENCY = 75f
        private const val GUITAR_MAX_FREQUENCY = 1100f
    }

    fun startListening() {
        if (listeningJob?.isActive == true) return

        _tunerState.update { it.copy(isListening = true) }
        pitchDetector.start()

        listeningJob = viewModelScope.launch {
            pitchDetector.pitchResults().collect { result ->
                if (result.frequency < GUITAR_MIN_FREQUENCY ||
                    result.frequency > GUITAR_MAX_FREQUENCY ||
                    result.confidence < 0.3f
                ) {
                    return@collect
                }

                val midiNote = frequencyToMidi(result.frequency)
                val note = frequencyMapper.midiNoteToNote(midiNote)
                val exactFreq = midiToFrequency(midiNote)
                val cents = frequencyToCents(result.frequency, exactFreq)
                val matchedString = findMatchingString(midiNote)

                _tunerState.update {
                    it.copy(
                        detectedFrequency = result.frequency,
                        detectedNote = note,
                        centsOffset = cents,
                        matchedString = matchedString,
                        confidence = result.confidence
                    )
                }
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        pitchDetector.stop()
        _tunerState.update {
            it.copy(
                isListening = false,
                detectedFrequency = 0f,
                detectedNote = null,
                centsOffset = 0f,
                matchedString = null,
                confidence = 0f
            )
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
