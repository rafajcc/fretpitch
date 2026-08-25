package com.fretpitch.domain.usecase

import com.fretpitch.domain.model.Exercise
import com.fretpitch.domain.model.Note
import javax.inject.Inject

data class ExerciseAttempt(
    val exercise: Exercise,
    val correct: Boolean
)

class CalculateStatsUseCase @Inject constructor() {

    operator fun invoke(attempts: List<ExerciseAttempt>, timeElapsedMs: Long): com.fretpitch.domain.model.SessionResult {
        val correct = attempts.count { it.correct }
        val incorrect = attempts.count { !it.correct }

        val noteStats = attempts.groupBy { it.exercise.note }.mapValues { (_, noteAttempts) ->
            com.fretpitch.domain.model.NoteResult(
                correct = noteAttempts.count { it.correct },
                incorrect = noteAttempts.count { !it.correct }
            )
        }

        val stringStats = attempts.groupBy { it.exercise.guitarString }.mapValues { (_, stringAttempts) ->
            com.fretpitch.domain.model.StringResult(
                correct = stringAttempts.count { it.correct },
                incorrect = stringAttempts.count { !it.correct }
            )
        }

        val combinationStats = attempts.groupBy {
            Pair(it.exercise.note, it.exercise.guitarString)
        }.mapValues { (_, comboAttempts) ->
            com.fretpitch.domain.model.NoteResult(
                correct = comboAttempts.count { it.correct },
                incorrect = comboAttempts.count { !it.correct }
            )
        }

        return com.fretpitch.domain.model.SessionResult(
            totalCorrect = correct,
            totalIncorrect = incorrect,
            timeElapsedMs = timeElapsedMs,
            noteStats = noteStats,
            stringStats = stringStats,
            combinationStats = combinationStats
        )
    }
}
