package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory {
    SAGLIK, SPOR, KISISEL
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: TaskCategory,
    val isRecurring: Boolean,
    val createdDate: String,
    val icon: String = "📝",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
)