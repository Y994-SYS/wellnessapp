package com.alkanyazilim.wellnesapp.data.repository

import com.alkanyazilim.wellnesapp.data.local.TaskCategory
import com.alkanyazilim.wellnesapp.data.local.TaskCompletionEntity
import com.alkanyazilim.wellnesapp.data.local.TaskDao
import com.alkanyazilim.wellnesapp.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class TaskWithStatus(
    val id: Int,
    val title: String,
    val category: TaskCategory,
    val isRecurring: Boolean,
    val isCompleted: Boolean,
    val icon: String,
    val reminderEnabled: Boolean,
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val currentStreak: Int,
    val bestStreak: Int
)

class TaskRepository(private val dao: TaskDao) {

    suspend fun addTask(
        title: String,
        category: TaskCategory,
        isRecurring: Boolean,
        todayDate: String,
        icon: String,
        reminderEnabled: Boolean = false,
        reminderHour: Int? = null,
        reminderMinute: Int? = null
    ): Long {
        return dao.insertTask(
            TaskEntity(
                title = title,
                category = category,
                isRecurring = isRecurring,
                createdDate = todayDate,
                icon = icon,
                reminderEnabled = reminderEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute
            )
        )
    }

    suspend fun deleteTaskById(taskId: Int) {
        dao.deleteTaskById(taskId)
    }

    suspend fun updateTask(taskId: Int, title: String, category: TaskCategory, isRecurring: Boolean, icon: String) {
        dao.updateTask(taskId, title, category, isRecurring, icon)
    }

    suspend fun updateReminder(taskId: Int, enabled: Boolean, hour: Int?, minute: Int?) {
        dao.updateReminder(taskId, enabled, hour, minute)
    }

    suspend fun getTaskEntity(taskId: Int): TaskEntity? = dao.getTaskById(taskId)

    suspend fun getTasksWithReminderEnabled(): List<TaskEntity> = dao.getTasksWithReminderEnabled()

    suspend fun setCompleted(taskId: Int, date: String, completed: Boolean) {
        dao.upsertCompletion(TaskCompletionEntity(taskId = taskId, date = date, isCompleted = completed))
        val history = dao.getCompletionsForTask(taskId)
        val (current, best) = TaskStreakCalculator.calculate(history)
        dao.updateStreak(taskId, current, best)
    }

    fun getTasksForDate(date: String): Flow<List<TaskWithStatus>> {
        return combine(dao.getAllTasks(), dao.getCompletionsForDate(date)) { tasks, completions ->
            val completionMap = completions.associateBy { it.taskId }

            tasks
                .filter { task -> task.isRecurring || task.createdDate <= date }
                .map { task ->
                    TaskWithStatus(
                        id = task.id,
                        title = task.title,
                        category = task.category,
                        isRecurring = task.isRecurring,
                        isCompleted = completionMap[task.id]?.isCompleted ?: false,
                        icon = task.icon,
                        reminderEnabled = task.reminderEnabled,
                        reminderHour = task.reminderHour,
                        reminderMinute = task.reminderMinute,
                        currentStreak = task.currentStreak,
                        bestStreak = task.bestStreak
                    )
                }
        }
    }
}
