package com.alkanyazilim.wellnesapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alkanyazilim.wellnesapp.MainActivity
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.utils.TaskAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "task_reminder_channel_v1"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(context).taskDao()
            val task = dao.getTaskById(taskId) ?: return@launch

            if (task.reminderEnabled) {
                showNotification(context, taskId, task.title, task.icon)

                if (task.reminderHour != null && task.reminderMinute != null) {
                    TaskAlarmScheduler.schedule(context, taskId, task.reminderHour, task.reminderMinute)
                }
            }
        }
    }

    private fun showNotification(context: Context, taskId: Int, title: String, icon: String) {
        createChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "tasks")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$icon $title")
            .setContentText("Bu görevi tamamlamayı unutma")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(4000 + taskId, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Görev Hatırlatıcı", NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Görev hatırlatıcı bildirimleri"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}