package com.fretpitch.domain.model

sealed class AppMode {
    data class OneNote(val note: Note) : AppMode()
    data class OneString(val guitarString: GuitarString) : AppMode()
    data object All : AppMode()
}
