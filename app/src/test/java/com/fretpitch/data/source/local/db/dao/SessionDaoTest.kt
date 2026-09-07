package com.fretpitch.data.source.local.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fretpitch.data.source.local.db.AppDatabase
import com.fretpitch.data.source.local.db.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: SessionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.sessionDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun writeSessionAndReadInList() = runBlocking {
        val session = SessionEntity(
            timestamp = 123456789L,
            totalCorrect = 10,
            totalIncorrect = 2,
            durationMs = 60000,
            modeInfo = "All"
        )
        dao.insertSession(session)
        val allSessions = dao.getAllSessions().first()
        assertEquals(1, allSessions.size)
        assertEquals(10, allSessions[0].totalCorrect)
        assertEquals("All", allSessions[0].modeInfo)
    }
}
