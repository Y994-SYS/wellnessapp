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
import android.os.Build
import android.os.IBinder
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

data class RunData(
    val isRunning: Boolean = false,
    val targetSteps: Int = 2000,
    val sessionSteps: Int = 0,
    val elapsedSeconds: Int = 0
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
        const val NOTIFICATION_ID = 3001
        const val ACTION_START = "com.alkanyazilim.wellnesapp.action.START_RUN"
        const val ACTION_STOP = "com.alkanyazilim.wellnesapp.action.STOP_RUN"
        const val EXTRA_TARGET_STEPS = "target_steps"
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
            ACTION_START -> startTracking(intent.getIntExtra(EXTRA_TARGET_STEPS, 2000))
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking(targetSteps: Int) {
        baseline = null
        sessionStartMillis = System.currentTimeMillis()
        RunSessionState.update {
            RunData(isRunning = true, targetSteps = targetSteps, sessionSteps = 0, elapsedSeconds = 0)
        }

        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0, targetSteps))

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                delay(1000)
                // NOT: Bu sayaç yalnızca CANLI EKRAN GÖSTERİMİ içindir.
                // Doze modu / CPU uykusu sırasında delay(1000) tam 1 saniyede
                // tetiklenmeyebilir, bu yüzden bu sayaç gerçek geçen süreden
                // az sayabilir. Kalıcı kayıt (saveSessionToHistory) bu sayaca
                // DEĞİL, gerçek saat farkına (wall-clock) göre hesaplanır.
                val realElapsed = ((System.currentTimeMillis() - sessionStartMillis) / 1000).toInt()
                RunSessionState.update { it.copy(elapsedSeconds = realElapsed) }
                val current = RunSessionState.data.value
                updateNotification(current.sessionSteps, current.targetSteps)
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

        // KRİTİK DÜZELTME: Süreyi tik sayacından (current.elapsedSeconds) değil,
        // gerçek başlangıç/bitiş saat farkından hesaplıyoruz. Tik sayacı, Doze
        // modu / arka plan CPU kısıtlamaları yüzünden gecikebilir ve gerçek
        // süreden birkaç dakika az gösterebilirdi. Wall-clock farkı bu duruma
        // bağışıktır ve her zaman doğru sonucu verir.
        val realDurationSeconds = ((endMillis - sessionStartMillis) / 1000).toInt()

        // Sadece en az birkaç saniye süren ve içinde anlamlı veri olan oturumları kaydet
        if (realDurationSeconds < 3) return

        val session = RunSessionEntity(
            startTimeMillis = sessionStartMillis,
            endTimeMillis = endMillis,
            steps = current.sessionSteps,
            targetSteps = current.targetSteps,
            durationSeconds = realDurationSeconds
        )
        // Servis kapanmadan önce senkron şekilde kaydediyoruz (runBlocking kısa ömürlü, güvenli)
        runBlocking {
            repository.saveSession(session)
        }
    }

    /**
     * Kullanıcı uygulamayı "son uygulamalar" listesinden kaydırıp kapatırsa bu çağrılır.
     * Aktif bir koşu varsa, o ana kadarki veriyi kaydedip servisi düzgünce kapatıyoruz.
     */
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

    private fun buildNotification(steps: Int, target: Int): android.app.Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Koşu takip ediliyor")
            .setContentText("$steps / $target adım")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(steps: Int, target: Int) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps, target))
    }

    private fun createChannel() {
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
        }
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        timerJob?.cancel()
        super.onDestroy()
    }
}