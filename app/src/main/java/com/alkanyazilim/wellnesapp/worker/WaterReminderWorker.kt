package com.alkanyazilim.wellnesapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alkanyazilim.wellnesapp.MainActivity
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import kotlinx.coroutines.flow.first

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID_SILENT = "water_reminder_channel_silent"
        const val CHANNEL_ID_SOUND_PREFIX = "water_reminder_channel_sound_"
        const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        val store = WaterDataStore(applicationContext)

        val soundEnabled = store.soundEnabled.first()
        if (soundEnabled) {
            showNotification(applicationContext)
        } else {
            showSilentNotification(applicationContext)
        }

        // Kendi kendini bir sonraki bildirim için yeniden zamanla (eski
        // AlarmScheduler.scheduleNext'in her tetiklenmede kendini yeniden
        // kurma davranışının aynısı).
        val enabled = store.reminderEnabled.first()
        if (enabled) {
            val interval = store.reminderIntervalMin.first()
            val start = store.reminderStartHour.first()
            val end = store.reminderEndHour.first()
            WaterReminderScheduler.scheduleNext(applicationContext, interval, start, end)
        }

        return Result.success()
    }

    // ---------- SESLİ BİLDİRİM ----------
    private fun showNotification(context: Context) {
        val soundUri = getSelectedSoundUri(context)
        val channelId = soundChannelId(soundUri)

        createSoundChannel(context, channelId, soundUri)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "water")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Su içme zamanı! 💧")
            .setContentText("Hedefine ulaşmak için bir bardak su içmeyi unutma.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun soundChannelId(soundUri: Uri): String {
        return "$CHANNEL_ID_SOUND_PREFIX${soundUri.toString().hashCode()}"
    }

    private fun createSoundChannel(context: Context, channelId: String, soundUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.deleteNotificationChannel("water_reminder_channel")

            if (manager.getNotificationChannel(channelId) != null) {
                cleanupOldSoundChannels(manager, keepChannelId = channelId)
                return
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Su İçme Hatırlatıcı (Sesli)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Su içme hatırlatıcı bildirimleri (sesli)"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
            cleanupOldSoundChannels(manager, keepChannelId = channelId)
        }
    }

    private fun cleanupOldSoundChannels(manager: NotificationManager, keepChannelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.notificationChannels
            .filter { it.id.startsWith(CHANNEL_ID_SOUND_PREFIX) && it.id != keepChannelId }
            .forEach { manager.deleteNotificationChannel(it.id) }
    }

    private fun getSelectedSoundUri(context: Context): Uri {
        val store = WaterDataStore(context)
        // NOT: Worker suspend fonksiyon içinde çalıştığı için runBlocking'e
        // gerek yok, ama bu yardımcı fonksiyon senkron kaldığı için istisna
        // olarak burada tutuyoruz — doWork zaten suspend context'te.
        val savedUriString = kotlinx.coroutines.runBlocking { store.reminderSoundUri.first() }
        return if (savedUriString.isNotBlank()) {
            Uri.parse(savedUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    // ---------- SESSİZ BİLDİRİM ----------
    private fun showSilentNotification(context: Context) {
        createSilentChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "water")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SILENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Su içme zamanı! 💧")
            .setContentText("Hedefine ulaşmak için bir bardak su içmeyi unutma.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createSilentChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID_SILENT) != null) return

            manager.deleteNotificationChannel("water_reminder_channel")

            val channel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Su İçme Hatırlatıcı (Sessiz)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sessiz su içme hatırlatıcı bildirimleri"
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }
}