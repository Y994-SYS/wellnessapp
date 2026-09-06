package com.alkanyazilim.wellnesapp.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

// Eski AlarmManager tabanlı TaskAlarmScheduler'ın yerine geçer.
object TaskReminderScheduler {

    private fun workName(taskId: Int) = "task_reminder_work_$taskId"

    fun schedule(context: Context, taskId: Int, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMillis = (next.timeInMillis - now.timeInMillis).coerceAtLeast(0)

        val inputData = Data.Builder().putInt("task_id", taskId).build()

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(taskId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}