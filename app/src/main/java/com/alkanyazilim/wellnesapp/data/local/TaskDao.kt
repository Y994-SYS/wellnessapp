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

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId")
    suspend fun getCompletionsForTask(taskId: Int): List<TaskCompletionEntity>

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("UPDATE tasks SET title = :title, category = :category, isRecurring = :isRecurring, icon = :icon WHERE id = :taskId")
    suspend fun updateTask(taskId: Int, title: String, category: TaskCategory, isRecurring: Boolean, icon: String)

    @Query("UPDATE tasks SET reminderEnabled = :enabled, reminderHour = :hour, reminderMinute = :minute WHERE id = :taskId")
    suspend fun updateReminder(taskId: Int, enabled: Boolean, hour: Int?, minute: Int?)

    // ---- YENİ: Yedekleme (export/import) için ----

    // Export: tüm tamamlama kayıtlarını tek seferde okur (Flow değil, anlık liste)
    @Query("SELECT * FROM task_completions")
    suspend fun getAllCompletionsOnce(): List<TaskCompletionEntity>

    // Import: geri yüklemeden önce mevcut verileri temizler
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM task_completions")
    suspend fun deleteAllCompletions()

    // Import: yedekteki ORİJİNAL id'lerle geri yükler (REPLACE ile çakışma olursa
    // üzerine yazar). Bu, task_completions tablosundaki taskId referanslarının
    // doğru görevlere işaret etmeye devam etmesi için ZORUNLU — id'ler otomatik
    // yeniden üretilseydi (autoGenerate ile normal insert), tüm tamamlama
    // kayıtları yanlış (ya da var olmayan) görevlere bağlanmış olurdu.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasksRestore(tasks: List<TaskEntity>)
    @Query("UPDATE tasks SET currentStreak = :current, bestStreak = :best WHERE id = :taskId")
    suspend fun updateStreak(taskId: Int, current: Int, best: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionsRestore(completions: List<TaskCompletionEntity>)
}
