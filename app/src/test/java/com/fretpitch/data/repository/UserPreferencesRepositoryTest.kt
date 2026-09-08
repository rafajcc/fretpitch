package com.fretpitch.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UserPreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var testScope: TestScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPreferencesRepositoryImpl

    @Before
    fun setup() {
        testScope = TestScope(StandardTestDispatcher())
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(temporaryFolder.newFolder(), "user_prefs.preferences_pb") }
        )
        repository = UserPreferencesRepositoryImpl(dataStore)
    }

    @After
    fun cleanup() {
        testScope.cancel()
    }

    @Test
    fun `default speed level is 3`() = runTest(testScope.testScheduler) {
        val speed = repository.speedLevel.first()
        assertEquals(3, speed)
    }

    @Test
    fun `update speed level persists value`() = runTest(testScope.testScheduler) {
        repository.updateSpeedLevel(8)
        val speed = repository.speedLevel.first()
        assertEquals(8, speed)
    }

    @Test
    fun `default app mode contains all notes and strings`() = runTest(testScope.testScheduler) {
        val mode = repository.appMode.first()
        assertEquals(Note.allNotes().toSet(), mode.selectedNotes)
        assertEquals(GuitarString.all().toSet(), mode.selectedStrings)
    }

    @Test
    fun `update app mode persists selection correctly`() = runTest(testScope.testScheduler) {
        val targetNotes = setOf(Note.C, Note.G, Note.E)
        val targetStrings = setOf(GuitarString.STRING_5, GuitarString.STRING_6)
        val targetMode = AppMode(targetNotes, targetStrings)
        
        repository.updateAppMode(targetMode)
        val mode = repository.appMode.first()
        
        assertEquals(targetNotes, mode.selectedNotes)
        assertEquals(targetStrings, mode.selectedStrings)
    }

    @Test
    fun `update include sharps persists value`() = runTest(testScope.testScheduler) {
        repository.updateIncludeSharps(false)
        val include = repository.includeSharps.first()
        assertEquals(false, include)
    }
}
