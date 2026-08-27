package com.fretpitch.domain.model

data class TunerState(
    val detectedFrequency: Float = 0f,
    val detectedNote: Note? = null,
    val centsOffset: Float = 0f,
    val matchedString: GuitarString? = null,
    val confidence: Float = 0f,
    val isListening: Boolean = false,
    val lastUpdateTimeMs: Long = 0L,
    val isStringTuned: Boolean = false
) {
    val isInTune: Boolean
        get() = detectedNote != null && kotlin.math.abs(centsOffset) <= 5f

    val isCloseToTune: Boolean
        get() = detectedNote != null && kotlin.math.abs(centsOffset) <= 15f

    val centsDisplay: String
        get() = if (detectedNote != null) {
            val sign = if (centsOffset > 0) "+" else ""
            "${sign}${"%.0f".format(centsOffset)} cents"
        } else ""

    val frequencyDisplay: String
        get() = if (detectedFrequency > 0f) "${"%.1f".format(detectedFrequency)} Hz" else ""

    val noteNameDisplay: String
        get() = detectedNote?.name?.replace("_SHARP", "#") ?: ""
}
