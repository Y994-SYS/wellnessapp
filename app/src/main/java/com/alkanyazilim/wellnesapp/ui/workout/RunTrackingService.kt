package com.alkanyazilim.wellnesapp.ui.workout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.alkanyazilim.wellnesapp.MainActivity
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.data.local.RunSessionEntity
import com.alkanyazilim.wellnesapp.data.repository.RunSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// YENİ: Koşu hedefi artık adım sayısı VEYA süre olabilir.
enum class RunGoalType { STEPS, DURATION }

data class RunData(
    val isRunning: Boolean = false,
    val goalType: RunGoalType = RunGoalType.STEPS,
    val targetSteps: Int = 2000,
    val targetDurationSeconds: Int = 900, // varsayılan 15 dk
    val sessionSteps: Int = 0,
    val elapsedSeconds: Int = 0,
    // Süre hedefine ulaşıldığında bildirim/ses birden fazla kez tetiklenmesin diye
    val goalReachedNotified: Boolean = false
)

object RunSessionState {
    private val _data = MutableStateFlow(RunData())
    val data: StateFlow<RunData> = _data

    fun update(transform: (RunData) -> RunData) {
        _data.value = transform(_data.value)
    }
}

class RunTrackingService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "run_tracking_channel"
        // YENİ: Süre hedefine ulaşıldığında gösterilen tek seferlik, SESLİ bildirim
        // için ayrı bir kanal. Ana takip kanalı (CHANNEL_ID) düşük öncelikli ve
        // sessiz kalmaya devam ediyor — bu ikisini karıştırmıyoruz.
        const val CHANNEL_ID_GOAL_REACHED = "run_goal_reached_channel"
        const val NOTIFICATION_ID = 3001
        const val NOTIFICATION_ID_GOAL_REACHED = 3002
        const val ACTION_START = "com.alkanyazilim.wellnesapp.action.START_RUN"
        const val ACTION_STOP = "com.alkanyazilim.wellnesapp.action.STOP_RUN"
        const val EXTRA_TARGET_STEPS = "target_steps"
        // YENİ
        const val EXTRA_GOAL_TYPE = "goal_type"
        const val EXTRA_TARGET_DURATION_SECONDS = "target_duration_seconds"
    }

    private var sensorManager: SensorManager? = null
    private var baseline: Int? = null
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private var sessionStartMillis: Long = 0L

    private lateinit var repository: RunSessionRepository

    override fun onCreate() {
        super.onCreate()
        val dao = AppDatabase.getInstance(applicationContext).runSessionDao()
        repository = RunSessionRepository(dao)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val goalTypeName = intent.getStringExtra(EXTRA_GOAL_TYPE) ?: RunGoalType.STEPS.name
                val goalType = runCatching { RunGoalType.valueOf(goalTypeName) }.getOrDefault(RunGoalType.STEPS)
                val targetSteps = intent.getIntExtra(EXTRA_TARGET_STEPS, 2000)
                val targetDuration = intent.getIntExtra(EXTRA_TARGET_DURATION_SECONDS, 900)
                startTracking(goalType, targetSteps, targetDuration)
            }
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking(goalType: RunGoalType, targetSteps: Int, targetDurationSeconds: Int) {
        baseline = null
        sessionStartMillis = System.currentTimeMillis()
        RunSessionState.update {
            RunData(
                isRunning = true,
                goalType = goalType,
                targetSteps = targetSteps,
                targetDurationSeconds = targetDurationSeconds,
                sessionSteps = 0,
                elapsedSeconds = 0,
                goalReachedNotified = false
            )
        }

        createChannels()
        startForeground(NOTIFICATION_ID, buildNotification(0, targetSteps, goalType, 0, targetDurationSeconds))

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                delay(1000)
                val realElapsed = ((System.currentTimeMillis() - sessionStartMillis) / 1000).toInt()
                RunSessionState.update { it.copy(elapsedSeconds = realElapsed) }
                val current = RunSessionState.data.value

                // YENİ: Süre hedefine ulaşıldıysa bir kez sesli/titreşimli bildirim gönder
                if (current.goalType == RunGoalType.DURATION &&
                    !current.goalReachedNotified &&
                    realElapsed >= current.targetDurationSeconds
                ) {
                    RunSessionState.update { it.copy(goalReachedNotified = true) }
                    notifyGoalReached()
                }

                updateNotification(current.sessionSteps, current.targetSteps, current.goalType, realElapsed, current.targetDurationSeconds)
            }
        }
    }

    private fun stopTracking() {
        saveSessionToHistory()
        sensorManager?.unregisterListener(this)
        timerJob?.cancel()
        RunSessionState.update { it.copy(isRunning = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun saveSessionToHistory() {
        val current = RunSessionState.data.value
        val endMillis = System.currentTimeMillis()
        val realDurationSeconds = ((endMillis - sessionStartMillis) / 1000).toInt()

        if (realDurationSeconds < 3) return

        val session = RunSessionEntity(
            startTimeMillis = sessionStartMillis,
            endTimeMillis = endMillis,
            steps = current.sessionSteps,
            targetSteps = current.targetSteps,
            durationSeconds = realDurationSeconds,
            // YENİ: Süre bazlı koşu hedefi artık geçmişe de doğru şekilde kaydediliyor
            goalType = current.goalType.name,
            targetDurationSeconds = current.targetDurationSeconds
        )
        runBlocking {
            repository.saveSession(session)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (RunSessionState.data.value.isRunning) {
            stopTracking()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val totalSinceBoot = event.values[0].toInt()
        if (baseline == null) baseline = totalSinceBoot
        val sessionSteps = (totalSinceBoot - (baseline ?: totalSinceBoot)).coerceAtLeast(0)
        RunSessionState.update { it.copy(sessionSteps = sessionSteps) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun buildNotification(
        steps: Int,
        target: Int,
        goalType: RunGoalType,
        elapsedSeconds: Int,
        targetDurationSeconds: Int
    ): android.app.Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (goalType == RunGoalType.DURATION) {
            val remaining = (targetDurationSeconds - elapsedSeconds).coerceAtLeast(0)
            val mm = remaining / 60
            val ss = remaining % 60
            "$steps adım · kalan süre %02d:%02d".format(mm, ss)
        } else {
            "$steps / $target adım"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Koşu takip ediliyor")
            .setContentText(contentText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(
        steps: Int,
        target: Int,
        goalType: RunGoalType,
        elapsedSeconds: Int,
        targetDurationSeconds: Int
    ) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps, target, goalType, elapsedSeconds, targetDurationSeconds))
    }

    // YENİ: Süre hedefine ulaşıldığında tetiklenen, sesli + titreşimli tek seferlik bildirim
    private fun notifyGoalReached() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 1, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_GOAL_REACHED)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Süre hedefine ulaştın! 🎉")
            .setContentText("Harika iş çıkardın. İstersen koşuya devam edebilir ya da durdurabilirsin.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_GOAL_REACHED, notification)

        // Ekstra: ekran kapalıyken de fark edilsin diye titreşim
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 300, 150, 300), -1)
        }
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Koşu Takibi",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Aktif koşu oturumu bildirimi"
                }
                manager.createNotificationChannel(channel)
            }

            if (manager.getNotificationChannel(CHANNEL_ID_GOAL_REACHED) == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val channel = NotificationChannel(
                    CHANNEL_ID_GOAL_REACHED,
                    "Koşu Hedefi Tamamlandı",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Süre hedefine ulaşıldığında sesli bildirim"
                    setSound(soundUri, android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        timerJob?.cancel()
        super.onDestroy()
    }
}