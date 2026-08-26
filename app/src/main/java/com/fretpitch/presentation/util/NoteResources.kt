package com.fretpitch.presentation.util

import androidx.annotation.StringRes
import com.fretpitch.domain.model.Note
import com.fretpitch.R

@StringRes
fun Note.nameResId(): Int = when (this) {
    Note.C -> R.string.note_C
    Note.C_SHARP -> R.string.note_C_SHARP
    Note.D -> R.string.note_D
    Note.D_SHARP -> R.string.note_D_SHARP
    Note.E -> R.string.note_E
    Note.F -> R.string.note_F
    Note.F_SHARP -> R.string.note_F_SHARP
    Note.G -> R.string.note_G
    Note.G_SHARP -> R.string.note_G_SHARP
    Note.A -> R.string.note_A
    Note.A_SHARP -> R.string.note_A_SHARP
    Note.B -> R.string.note_B
}
