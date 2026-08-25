package com.fretpitch.domain.model

data class SessionResult(
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val timeElapsedMs: Long,
    val noteStats: Map<Note, NoteResult>,
    val stringStats: Map<GuitarString, StringResult>,
    val combinationStats: Map<Pair<Note, GuitarString>, NoteResult>
) {
    val totalAttempts: Int get() = totalCorrect + totalIncorrect
    val accuracy: Float
        get() = if (totalAttempts > 0) totalCorrect.toFloat() / totalAttempts else 0f
    val formattedTime: String
        get() {
            val totalSeconds = timeElapsedMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class NoteResult(
    val correct: Int,
    val incorrect: Int
) {
    val total: Int get() = correct + incorrect
    val accuracy: Float
        get() = if (total > 0) correct.toFloat() / total else 0f
}

data class StringResult(
    val correct: Int,
    val incorrect: Int
) {
    val total: Int get() = correct + incorrect
    val accuracy: Float
        get() = if (total > 0) correct.toFloat() / total else 0f
}
