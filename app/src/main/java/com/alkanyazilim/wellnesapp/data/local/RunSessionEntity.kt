package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val steps: Int,
    val targetSteps: Int,
    val durationSeconds: Int
)