package com.fretpitch.presentation.model

import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.Exercise
import com.fretpitch.domain.model.SessionResult
import com.fretpitch.domain.usecase.ExerciseAttempt

data class MainUiState(
    val mode: AppMode = AppMode.All,
    val includeSharps: Boolean = true,
    val speedLevel: Int = 5,
    val isPlaying: Boolean = false,
    val currentExercise: Exercise? = null,
    val feedback: FeedbackState = FeedbackState.None,
    val attempts: List<ExerciseAttempt> = emptyList(),
    val sessionResult: SessionResult? = null,
    val startTimeMs: Long = 0L,
    val hasMicPermission: Boolean = false
) {
    val intervalSeconds: Int
        get() = 11 - speedLevel

    val intervalMs: Long
        get() = intervalSeconds * 1000L

    val speedDisplay: String
        get() = "${intervalSeconds}s"
}

sealed class FeedbackState {
    data object None : FeedbackState()
    data object Correct : FeedbackState()
    data object Incorrect : FeedbackState()
    data object Listening : FeedbackState()
}
