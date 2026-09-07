package com.fretpitch.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import com.fretpitch.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val SPEED_LEVEL = intPreferencesKey("speed_level")
        val MODE_TYPE = stringPreferencesKey("mode_type")
        val MODE_NOTE = stringPreferencesKey("mode_note")
        val MODE_STRING = intPreferencesKey("mode_string")
        val INCLUDE_SHARPS = booleanPreferencesKey("include_sharps")
    }

    override val speedLevel: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SPEED_LEVEL] ?: 3
    }

    override val appMode: Flow<AppMode> = dataStore.data.map { preferences ->
        val type = preferences[PreferencesKeys.MODE_TYPE] ?: "All"
        when (type) {
            "OneNote" -> {
                val noteName = preferences[PreferencesKeys.MODE_NOTE] ?: Note.E.name
                val note = try { Note.valueOf(noteName) } catch (e: Exception) { Note.E }
                AppMode.OneNote(note)
            }
            "OneString" -> {
                val stringNum = preferences[PreferencesKeys.MODE_STRING] ?: 1
                val guitarString = GuitarString.fromNumber(stringNum) ?: GuitarString.STRING_1
                AppMode.OneString(guitarString)
            }
            else -> AppMode.All
        }
    }

    override val includeSharps: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INCLUDE_SHARPS] ?: true
    }

    override suspend fun updateSpeedLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPEED_LEVEL] = level
        }
    }

    override suspend fun updateAppMode(mode: AppMode) {
        dataStore.edit { preferences ->
            when (mode) {
                is AppMode.OneNote -> {
                    preferences[PreferencesKeys.MODE_TYPE] = "OneNote"
                    preferences[PreferencesKeys.MODE_NOTE] = mode.note.name
                }
                is AppMode.OneString -> {
                    preferences[PreferencesKeys.MODE_TYPE] = "OneString"
                    preferences[PreferencesKeys.MODE_STRING] = mode.guitarString.number
                }
                is AppMode.All -> {
                    preferences[PreferencesKeys.MODE_TYPE] = "All"
                }
            }
        }
    }

    override suspend fun updateIncludeSharps(include: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_SHARPS] = include
        }
    }
}
