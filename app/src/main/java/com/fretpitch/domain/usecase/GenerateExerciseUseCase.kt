package com.fretpitch.domain.usecase

import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.Exercise
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import javax.inject.Inject
import kotlin.math.pow

class GenerateExerciseUseCase @Inject constructor() {

    companion object {
        // Physical limit for this practice app: First position + Octave
        private const val MAX_FRET = 12
    }

    operator fun invoke(
        mode: AppMode,
        includeSharps: Boolean,
        excludeNote: Note? = null,
        excludeString: GuitarString? = null
    ): Exercise {
        // Filter selection by sharps preference if necessary
        val notesPool = if (includeSharps) {
            mode.selectedNotes
        } else {
            mode.selectedNotes.filter { !it.isSharp }
        }
        
        val stringsPool = mode.selectedStrings

        val validCombinations = notesPool.flatMap { note ->
            stringsPool.mapNotNull { string -> createExerciseIfValid(note, string) }
        }

        val filtered = if (excludeNote != null && excludeString != null) {
            validCombinations.filter {
                !(it.note == excludeNote && it.guitarString == excludeString)
            }
        } else {
            validCombinations
        }

        val pool = filtered.ifEmpty { validCombinations }

        require(pool.isNotEmpty()) {
            "No valid exercises found for the given selection and options"
        }

        return pool.random()
    }

    private fun createExerciseIfValid(note: Note, guitarString: GuitarString): Exercise? {
        val openNoteSemitone = guitarString.openNoteMidi % 12
        val targetSemitone = note.semitone
        
        // Calculate the lowest fret for this note (0-11)
        val fret = (targetSemitone - openNoteSemitone + 12) % 12
        
        // We keep it predictable (first position 0-11). 
        // Validation remains for physical sanity check.
        if (fret > MAX_FRET) return null

        val targetMidi = guitarString.openNoteMidi + fret
        val expectedFrequency = midiToFrequency(targetMidi)

        return Exercise(
            note = note,
            guitarString = guitarString,
            expectedFrequency = expectedFrequency
        )
    }

    private fun midiToFrequency(midi: Int): Float {
        return 440f * 2f.pow((midi - 69) / 12f)
    }
}
