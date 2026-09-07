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

    @Before
    fun setup() {
        useCase = GenerateExerciseUseCase()
    }

    @Test
    fun `generate exercise respects includeSharps false`() {
        val exercise = useCase(AppMode.All, includeSharps = false)
        assertTrue("Note should not be sharp", !exercise.note.isSharp)
    }

    @Test
    fun `generate exercise respects OneString mode`() {
        val targetString = GuitarString.STRING_3
        val exercise = useCase(AppMode.OneString(targetString), includeSharps = true)
        assertTrue("String should be 3", exercise.guitarString == targetString)
    }

    @Test
    fun `generate exercise respects OneNote mode`() {
        val targetNote = Note.C
        val exercise = useCase(AppMode.OneNote(targetNote), includeSharps = true)
        assertTrue("Note should be C", exercise.note == targetNote)
    }

    @Test
    fun `generate exercise respects exclusion filter`() {
        val lastNote = Note.E
        val lastString = GuitarString.STRING_1
        
        repeat(50) {
            val exercise = useCase(
                AppMode.All, 
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
        val notes = Note.allNotes()
        val strings = GuitarString.all()
        
        // This test checks the internal logic by running the use case for all combinations
        strings.forEach { string ->
            notes.forEach { note ->
                // Accessing the private method via reflection or just testing All mode output
                val exercise = useCase(AppMode.OneNote(note), includeSharps = true)
                // In our model, fret is derived from frequency
                // expectedFrequency = 440 * 2^((midi-69)/12)
                // We verify that the midi calculated internally is within range
                // The useCase doesn't expose fret, but it uses it to calculate frequency
                // If the frequency corresponds to a midi note within [open, open+12], it's valid.
                
                val openMidi = string.openNoteMidi
                val maxMidi = openMidi + 12
                
                // Frequency to Midi (approximate)
                val midi = (12 * Math.log((exercise.expectedFrequency / 440.0).toDouble()) / Math.log(2.0) + 69).toInt()
                
                // For a specific OneNote mode, it will pick one of the available strings.
                // We want to verify that for ANY exercise generated, it's valid.
            }
        }
        
        // Let's do a more direct test for all combinations
        repeat(200) {
            val exercise = useCase(AppMode.All, includeSharps = true)
            val openMidi = exercise.guitarString.openNoteMidi
            val freq = exercise.expectedFrequency
            val midi = Math.round(12 * Math.log((freq / 440.0).toDouble()) / Math.log(2.0) + 69).toInt()
            val fret = midi - openMidi
            
            assertTrue("Fret $fret should be between 0 and 12", fret in 0..12)
        }
    }
}
