package com.alkanyazilim.wellnesapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.repository.HealthConnectManager
import kotlinx.coroutines.flow.first

// Widget'ın adım verisini arka planda tazeler. Health Connect'e erişim izni
// yoksa 0 yazar (widget kilitli izin ekranını göstermez, sessizce 0/hedef kalır).
class StepsWidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val healthConnectManager = HealthConnectManager(applicationContext)
            val userPreferences = UserPreferences(applicationContext)

            val steps = if (healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()) {
                healthConnectManager.readTodaySteps()
            } else {
                0L
            }
            val goal = userPreferences.stepGoal.first()

            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(StepsWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[StepsWidgetKeys.STEPS_KEY] = steps.toInt()
                    prefs[StepsWidgetKeys.GOAL_KEY] = goal
                }
            }

            StepsWidget().updateAll(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}