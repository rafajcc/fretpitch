package com.fretpitch.domain.usecase

import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.Exercise
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import javax.inject.Inject
import kotlin.math.pow

class GenerateExerciseUseCase @Inject constructor() {

    operator fun invoke(mode: AppMode, includeSharps: Boolean): Exercise {
        val notes = if (includeSharps) Note.allNotes() else Note.naturalNotes()
        val strings = GuitarString.all()

        val validCombinations = when (mode) {
            is AppMode.OneNote -> {
                strings.mapNotNull { string -> createExerciseIfValid(mode.note, string) }
            }
            is AppMode.OneString -> {
                notes.mapNotNull { note -> createExerciseIfValid(note, mode.guitarString) }
            }
            is AppMode.All -> {
                notes.flatMap { note ->
                    strings.mapNotNull { string -> createExerciseIfValid(note, string) }
                }
            }
        }

        require(validCombinations.isNotEmpty()) {
            "No valid exercises found for the given mode and options"
        }

        return validCombinations.random()
    }

    private fun createExerciseIfValid(note: Note, guitarString: GuitarString): Exercise? {
        val fret = (note.semitone - guitarString.openNoteMidi % 12 + 12) % 12
        if (fret > 12) return null

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
