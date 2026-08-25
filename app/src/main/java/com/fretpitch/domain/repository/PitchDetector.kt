package com.fretpitch.domain.repository

import kotlinx.coroutines.flow.Flow

interface PitchDetector {
    fun start()
    fun stop()
    fun pitchResults(): Flow<PitchResult>
}

data class PitchResult(
    val frequency: Float,
    val confidence: Float
)
