package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_completions", primaryKeys = ["taskId", "date"])
data class TaskCompletionEntity(
    val taskId: Int,
    val date: String, // "yyyy-MM-dd"
    val isCompleted: Boolean
)