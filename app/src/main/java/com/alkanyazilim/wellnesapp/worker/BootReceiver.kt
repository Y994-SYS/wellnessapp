package com.alkanyazilim.wellnesapp.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.utils.TaskAlarmScheduler
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val store = WaterDataStore(context)
            CoroutineScope(Dispatchers.IO).launch {
                if (store.reminderEnabled.first()) {
                    val interval = store.reminderIntervalMin.first()
                    val start = store.reminderStartHour.first()
                    val end = store.reminderEndHour.first()
                    AlarmScheduler.scheduleNext(context, interval, start, end)
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            val taskDao = AppDatabase.getInstance(context).taskDao()
            val tasksWithReminder = taskDao.getTasksWithReminderEnabled()
            tasksWithReminder.forEach { task ->
                if (task.reminderHour != null && task.reminderMinute != null) {
                    TaskAlarmScheduler.schedule(context, task.id, task.reminderHour, task.reminderMinute)
                }
            }
        }
    }
}