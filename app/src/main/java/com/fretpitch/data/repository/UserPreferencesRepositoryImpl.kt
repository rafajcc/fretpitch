package com.fretpitch.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        val SELECTED_NOTES = stringSetPreferencesKey("selected_notes")
        val SELECTED_STRINGS = stringSetPreferencesKey("selected_strings")
        val INCLUDE_SHARPS = booleanPreferencesKey("include_sharps")
    }

    override val speedLevel: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SPEED_LEVEL] ?: 3
    }

    override val appMode: Flow<AppMode> = dataStore.data.map { preferences ->
        val notesSet = preferences[PreferencesKeys.SELECTED_NOTES]
        val stringsSet = preferences[PreferencesKeys.SELECTED_STRINGS]

        val selectedNotes = if (notesSet == null) {
            Note.allNotes().toSet()
        } else {
            notesSet.mapNotNull { name ->
                try { Note.valueOf(name) } catch (e: Exception) { null }
            }.toSet().ifEmpty { Note.allNotes().toSet() }
        }

        val selectedStrings = if (stringsSet == null) {
            GuitarString.all().toSet()
        } else {
            stringsSet.mapNotNull { num ->
                GuitarString.fromNumber(num.toIntOrNull() ?: -1)
            }.toSet().ifEmpty { GuitarString.all().toSet() }
        }

        AppMode(selectedNotes, selectedStrings)
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
            preferences[PreferencesKeys.SELECTED_NOTES] = mode.selectedNotes.map { it.name }.toSet()
            preferences[PreferencesKeys.SELECTED_STRINGS] = mode.selectedStrings.map { it.number.toString() }.toSet()
        }
    }

    override suspend fun updateIncludeSharps(include: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_SHARPS] = include
        }
    }
}
