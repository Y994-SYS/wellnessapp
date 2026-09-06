package com.alkanyazilim.wellnesapp.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.alkanyazilim.wellnesapp.worker.TaskReminderScheduler
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // NOT: Su hatırlatıcısı artık WorkManager ile zamanlanıyor — WorkManager'ın
        // kendi veritabanı reboot'ta korunduğu için burada yeniden kurmaya gerek yok.
        CoroutineScope(Dispatchers.IO).launch {
            val taskDao = AppDatabase.getInstance(context).taskDao()
            val tasksWithReminder = taskDao.getTasksWithReminderEnabled()
            tasksWithReminder.forEach { task ->
                if (task.reminderHour != null && task.reminderMinute != null) {
                    TaskReminderScheduler.schedule(context, task.id, task.reminderHour, task.reminderMinute)
                }
            }
        }
    }
}