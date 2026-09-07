package com.fretpitch.domain.repository

import com.fretpitch.domain.model.AppMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val speedLevel: Flow<Int>
    val appMode: Flow<AppMode>
    val includeSharps: Flow<Boolean>

    suspend fun updateSpeedLevel(level: Int)
    suspend fun updateAppMode(mode: AppMode)
    suspend fun updateIncludeSharps(include: Boolean)
}
