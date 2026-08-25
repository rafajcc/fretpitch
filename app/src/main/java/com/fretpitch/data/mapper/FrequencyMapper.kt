package com.fretpitch.data.mapper

import com.fretpitch.domain.model.Note
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

@Singleton
class FrequencyMapper @Inject constructor() {

    companion object {
        private const val A4_FREQUENCY = 440f
        private const val A4_MIDI = 69
    }

    fun frequencyToMidiNote(frequency: Float): Int? {
        if (frequency <= 0f) return null
        return (12f * log2(frequency / A4_FREQUENCY) + A4_MIDI).roundToInt()
    }

    fun midiNoteToFrequency(midi: Int): Float {
        return A4_FREQUENCY * 2f.pow((midi - A4_MIDI) / 12f)
    }

    fun isNoteCorrect(detectedFreq: Float, targetFreq: Float, toleranceCents: Float = 50f): Boolean {
        if (detectedFreq <= 0f || targetFreq <= 0f) return false

        val centsOff = 1200f * log2(detectedFreq / targetFreq)
        return abs(centsOff) <= toleranceCents
    }

    fun midiNoteToNote(midiNote: Int): Note? {
        val noteIndex = midiNote % 12
        return when (noteIndex) {
            0 -> Note.C
            1 -> Note.C_SHARP
            2 -> Note.D
            3 -> Note.D_SHARP
            4 -> Note.E
            5 -> Note.F
            6 -> Note.F_SHARP
            7 -> Note.G
            8 -> Note.G_SHARP
            9 -> Note.A
            10 -> Note.A_SHARP
            11 -> Note.B
            else -> null
        }
    }
}
