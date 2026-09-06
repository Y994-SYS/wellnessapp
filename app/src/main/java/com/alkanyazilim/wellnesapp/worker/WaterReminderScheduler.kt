package com.alkanyazilim.wellnesapp.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

// AlarmManager tabanlı eski AlarmScheduler'ın yerine geçer. WorkManager kullanmanın
// avantajı: SCHEDULE_EXACT_ALARM izni gerekmez, ve zamanlanmış iş telefon yeniden
// başlasa bile WorkManager'ın kendi veritabanında kalıcı olarak saklanır — BootReceiver'da
// ayrıca yeniden kurmaya gerek kalmaz.
object WaterReminderScheduler {

    private const val WORK_NAME = "water_reminder_work"

    fun scheduleNext(context: Context, intervalMinutes: Int, startHour: Int, endHour: Int) {
        val now = Calendar.getInstance()
        var next = (now.clone() as Calendar).apply {
            add(Calendar.MINUTE, intervalMinutes)
        }

        val nextHour = next.get(Calendar.HOUR_OF_DAY)
        if (nextHour !in startHour until endHour) {
            next = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Aynı gece yarısı düzeltmesi: gerçek zaman karşılaştırması
            if (!next.after(now)) {
                next.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delayMillis = (next.timeInMillis - now.timeInMillis).coerceAtLeast(0)

        val request = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}