package com.fretpitch.domain.usecase

import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateExerciseUseCaseTest {

    private lateinit var useCase: GenerateExerciseUseCase
    private val allNotes = Note.allNotes().toSet()
    private val allStrings = GuitarString.all().toSet()

    @Before
    fun setup() {
        useCase = GenerateExerciseUseCase()
    }

    @Test
    fun `generate exercise respects includeSharps false`() {
        val mode = AppMode(allNotes, allStrings)
        val exercise = useCase(mode, includeSharps = false)
        assertTrue("Note should not be sharp", !exercise.note.isSharp)
    }

    @Test
    fun `generate exercise respects selected strings`() {
        val targetString = GuitarString.STRING_3
        val mode = AppMode(allNotes, setOf(targetString))
        val exercise = useCase(mode, includeSharps = true)
        assertTrue("String should be 3", exercise.guitarString == targetString)
    }

    @Test
    fun `generate exercise respects selected notes`() {
        val targetNote = Note.C
        val mode = AppMode(setOf(targetNote), allStrings)
        val exercise = useCase(mode, includeSharps = true)
        assertTrue("Note should be C", exercise.note == targetNote)
    }

    @Test
    fun `generate exercise respects exclusion filter`() {
        val lastNote = Note.E
        val lastString = GuitarString.STRING_1
        val mode = AppMode(allNotes, allStrings)
        
        repeat(50) {
            val exercise = useCase(
                mode, 
                includeSharps = true,
                excludeNote = lastNote,
                excludeString = lastString
            )
            assertNotEquals("Should not repeat last combination", 
                Pair(lastNote, lastString), Pair(exercise.note, exercise.guitarString))
        }
    }

    @Test
    fun `all possible exercises are within 0 to 12 frets range`() {
        val mode = AppMode(allNotes, allStrings)
        
        repeat(200) {
            val exercise = useCase(mode, includeSharps = true)
            val openMidi = exercise.guitarString.openNoteMidi
            val freq = exercise.expectedFrequency
            // Simplified reverse mapping to verify fret
            val midi = Math.round(12 * Math.log((freq / 440.0).toDouble()) / Math.log(2.0) + 69).toInt()
            val fret = midi - openMidi
            
            assertTrue("Fret $fret should be between 0 and 12", fret in 0..12)
        }
    }
}
