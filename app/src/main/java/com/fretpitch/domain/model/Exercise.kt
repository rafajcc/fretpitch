package com.fretpitch.domain.model

data class Exercise(
    val note: Note,
    val guitarString: GuitarString,
    val expectedFrequency: Float
) {
    val displayText: String
        get() = "${note.displayName} en la cuerda ${guitarString.number}"
}
