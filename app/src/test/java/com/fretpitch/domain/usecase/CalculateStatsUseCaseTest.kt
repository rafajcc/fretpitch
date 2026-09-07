package com.fretpitch.domain.usecase

import com.fretpitch.domain.model.Exercise
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateStatsUseCaseTest {

    private lateinit var useCase: CalculateStatsUseCase

    @Before
    fun setup() {
        useCase = CalculateStatsUseCase()
    }

    @Test
    fun `empty attempts results in zero stats`() {
        val result = useCase(emptyList(), 1000L)
        
        assertEquals(0, result.totalCorrect)
        assertEquals(0, result.totalIncorrect)
        assertEquals(0.0f, result.accuracy, 0.001f)
        assertEquals("0:01", result.formattedTime)
    }

    @Test
    fun `calculates basic accuracy correctly`() {
        val exercise = Exercise(Note.C, GuitarString.STRING_1, 261.63f)
        val attempts = listOf(
            ExerciseAttempt(exercise, true),
            ExerciseAttempt(exercise, true),
            ExerciseAttempt(exercise, false),
            ExerciseAttempt(exercise, false)
        )
        
        val result = useCase(attempts, 120000L) // 2 minutes
        
        assertEquals(2, result.totalCorrect)
        assertEquals(2, result.totalIncorrect)
        assertEquals(0.5f, result.accuracy, 0.001f)
        assertEquals("2:00", result.formattedTime)
    }

    @Test
    fun `formats time correctly for long sessions`() {
        val result = useCase(emptyList(), 65000L) // 1 min 5 sec
        assertEquals("1:05", result.formattedTime)
    }

    @Test
    fun `stats per note are calculated correctly`() {
        val exC = Exercise(Note.C, GuitarString.STRING_1, 261.63f)
        val exD = Exercise(Note.D, GuitarString.STRING_1, 293.66f)
        val attempts = listOf(
            ExerciseAttempt(exC, true),
            ExerciseAttempt(exC, false),
            ExerciseAttempt(exD, true)
        )
        
        val result = useCase(attempts, 1000L)
        
        assertEquals(1, result.noteStats[Note.C]?.correct)
        assertEquals(1, result.noteStats[Note.C]?.incorrect)
        assertEquals(1, result.noteStats[Note.D]?.correct)
        assertEquals(0, result.noteStats[Note.D]?.incorrect)
    }
}
