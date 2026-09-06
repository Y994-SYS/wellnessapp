package com.alkanyazilim.wellnesapp.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object StepsWidgetScheduler {

    private const val PERIODIC_WORK_NAME = "steps_widget_periodic_update"

    // Widget hiçbir kullanıcı etkileşimi olmasa bile arka planda 30 dakikada
    // bir tazelensin diye. 30 dk, Android'in WorkManager'a izin verdiği en
    // sık pratik periyottur.
    fun schedulePeriodicUpdates(context: Context) {
        val request = PeriodicWorkRequestBuilder<StepsWidgetUpdateWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // Kullanıcı uygulamayı her açtığında widget'ı hemen tazelemek için.
    fun requestImmediateUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<StepsWidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}