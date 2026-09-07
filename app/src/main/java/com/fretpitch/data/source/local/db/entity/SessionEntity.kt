package com.fretpitch.data.source.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val durationMs: Long,
    val modeInfo: String
)
