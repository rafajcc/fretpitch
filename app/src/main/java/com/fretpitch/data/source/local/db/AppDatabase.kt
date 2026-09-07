package com.fretpitch.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fretpitch.data.source.local.db.dao.SessionDao
import com.fretpitch.data.source.local.db.entity.SessionEntity

@Database(entities = [SessionEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "fretpitch_db"
    }
}
