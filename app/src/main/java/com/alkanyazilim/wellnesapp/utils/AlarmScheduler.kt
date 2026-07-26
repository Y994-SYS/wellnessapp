package com.alkanyazilim.wellnesapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alkanyazilim.wellnesapp.worker.WaterReminderReceiver
import java.util.Calendar

object AlarmScheduler {

    private const val REQUEST_CODE = 1001

    fun scheduleNext(context: Context, intervalMinutes: Int, startHour: Int, endHour: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = Calendar.getInstance()
        var next = (now.clone() as Calendar).apply {
            add(Calendar.MINUTE, intervalMinutes)
        }

        // Aktif saat aralığı dışına düşerse, ertesi günün başlangıç saatine ayarla
        val nextHour = next.get(Calendar.HOUR_OF_DAY)
        if (nextHour !in startHour until endHour) {
            next = (now.clone() as Calendar).apply {
                if (nextHour >= endHour) add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
        }

        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Kullanıcıyı izin ekranına yönlendirmek için ayarlarda kontrol et
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next.timeInMillis,
            pendingIntent
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}