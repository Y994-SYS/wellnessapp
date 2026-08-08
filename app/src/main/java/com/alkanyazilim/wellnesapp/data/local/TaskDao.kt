package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE reminderEnabled = 1")
    suspend fun getTasksWithReminderEnabled(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(completion: TaskCompletionEntity)

    @Query("SELECT * FROM task_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<TaskCompletionEntity>>

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("UPDATE tasks SET title = :title, category = :category, isRecurring = :isRecurring, icon = :icon WHERE id = :taskId")
    suspend fun updateTask(taskId: Int, title: String, category: TaskCategory, isRecurring: Boolean, icon: String)

    @Query("UPDATE tasks SET reminderEnabled = :enabled, reminderHour = :hour, reminderMinute = :minute WHERE id = :taskId")
    suspend fun updateReminder(taskId: Int, enabled: Boolean, hour: Int?, minute: Int?)
}