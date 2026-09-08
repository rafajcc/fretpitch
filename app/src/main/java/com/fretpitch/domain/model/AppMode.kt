package com.fretpitch.domain.model

data class AppMode(
    val selectedNotes: Set<Note> = Note.allNotes().toSet(),
    val selectedStrings: Set<GuitarString> = GuitarString.all().toSet()
)
