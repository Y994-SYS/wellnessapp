package com.alkanyazilim.wellnesapp.ui.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.data.local.TaskCategory
import com.alkanyazilim.wellnesapp.data.repository.TaskRepository
import com.alkanyazilim.wellnesapp.data.repository.TaskWithStatus
import com.alkanyazilim.wellnesapp.utils.TaskAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TasksViewModel(
    private val context: Context,
    private val repository: TaskRepository
) : ViewModel() {

    private fun today() = LocalDate.now().toString()

    val tasks: StateFlow<List<TaskWithStatus>> = repository.getTasksForDate(today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(
        title: String,
        category: TaskCategory,
        isRecurring: Boolean,
        icon: String,
        reminderEnabled: Boolean = false,
        reminderHour: Int? = null,
        reminderMinute: Int? = null
    ) {
        viewModelScope.launch {
            val newId = repository.addTask(title, category, isRecurring, today(), icon, reminderEnabled, reminderHour, reminderMinute)
            if (reminderEnabled && reminderHour != null && reminderMinute != null) {
                TaskAlarmScheduler.schedule(context, newId.toInt(), reminderHour, reminderMinute)
            }
        }
    }

    fun updateTask(taskId: Int, title: String, category: TaskCategory, isRecurring: Boolean, icon: String) {
        viewModelScope.launch {
            repository.updateTask(taskId, title, category, isRecurring, icon)
        }
    }

    fun setReminder(taskId: Int, enabled: Boolean, hour: Int?, minute: Int?) {
        viewModelScope.launch {
            repository.updateReminder(taskId, enabled, hour, minute)
            if (enabled && hour != null && minute != null) {
                TaskAlarmScheduler.schedule(context, taskId, hour, minute)
            } else {
                TaskAlarmScheduler.cancel(context, taskId)
            }
        }
    }

    fun toggleCompletion(taskId: Int, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.setCompleted(taskId, today(), !currentlyCompleted)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            TaskAlarmScheduler.cancel(context, taskId)
            repository.deleteTaskById(taskId)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dao = AppDatabase.getInstance(context).taskDao()
            val repository = TaskRepository(dao)
            return TasksViewModel(context.applicationContext, repository) as T
        }
    }
}