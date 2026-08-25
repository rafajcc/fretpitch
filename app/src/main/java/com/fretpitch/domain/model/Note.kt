package com.fretpitch.domain.model

enum class Note(val displayName: String, val semitone: Int) {
    C("Do", 0),
    C_SHARP("Do#", 1),
    D("Re", 2),
    D_SHARP("Re#", 3),
    E("Mi", 4),
    F("Fa", 5),
    F_SHARP("Fa#", 6),
    G("Sol", 7),
    G_SHARP("Sol#", 8),
    A("La", 9),
    A_SHARP("La#", 10),
    B("Si", 11);

    val isSharp: Boolean
        get() = name.endsWith("_SHARP")

    companion object {
        fun naturalNotes(): List<Note> = entries.filter { !it.isSharp }
        fun allNotes(): List<Note> = entries.toList()
    }
}
