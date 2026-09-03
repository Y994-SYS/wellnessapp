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
    val durationSeconds: Int,
    // YENİ (şema v5): Koşu artık adım VEYA süre hedefiyle başlatılabiliyor.
    // Enum yerine String tutuyoruz ki ekstra bir Room TypeConverter gerekmesin —
    // değer her zaman RunGoalType.STEPS.name / RunGoalType.DURATION.name olarak yazılır.
    val goalType: String = "STEPS",
    // goalType == "DURATION" ise anlamlı (hedeflenen saniye), değilse 0
    val targetDurationSeconds: Int = 0
)