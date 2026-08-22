package com.alkanyazilim.wellnesapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alkanyazilim.wellnesapp.MainActivity
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WaterReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID_SILENT = "water_reminder_channel_silent"
        // NOT: Bu artık sabit bir kanal ID'si DEĞİL, bir ÖN EK (prefix).
        // Gerçek kanal ID'si, seçilen ses URI'sine göre dinamik üretilir
        // (bkz. soundChannelId()). Böylece kullanıcı farklı bir ses seçtiğinde
        // Android'in eski kanalı "sil + yeniden oluştur" ile güncellemesine güvenmek
        // yerine, doğrudan hiç dokunulmamış YENİ bir kanal kullanılır. Bazı Android
        // sürümlerinde/cihazlarında sil+yeniden oluştur güvenilir çalışmıyor ve eski
        // ses "yapışık" kalabiliyor — bu yaklaşım o sorunu kökten ortadan kaldırır.
        const val CHANNEL_ID_SOUND_PREFIX = "water_reminder_channel_sound_"
        const val NOTIFICATION_ID = 2001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val store = WaterDataStore(context)

        CoroutineScope(Dispatchers.IO).launch {
            val soundEnabled = store.soundEnabled.first()

            if (soundEnabled) {
                showNotification(context)
            } else {
                showSilentNotification(context)
            }

            val interval = store.reminderIntervalMin.first()
            val start = store.reminderStartHour.first()
            val end = store.reminderEndHour.first()
            val enabled = store.reminderEnabled.first()
            if (enabled) {
                AlarmScheduler.scheduleNext(context, interval, start, end)
            }
        }
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

    // Seçilen ses URI'sine özgü, benzersiz bir kanal ID'si üretir.
    // Aynı ses için her zaman aynı ID döner (gereksiz kanal çoğalmasını önler),
    // ama farklı bir ses seçildiğinde otomatik olarak yepyeni bir ID üretir.
    private fun soundChannelId(soundUri: Uri): String {
        return "$CHANNEL_ID_SOUND_PREFIX${soundUri.toString().hashCode()}"
    }

    private fun createSoundChannel(context: Context, channelId: String, soundUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Eski, artık kullanılmayan sabit kanal adını temizle (geçmiş sürümlerden kalan)
            manager.deleteNotificationChannel("water_reminder_channel")

            // Bu ses için kanal zaten oluşturulmuşsa tekrar oluşturmaya gerek yok
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

            // Artık kullanılmayan eski ses kanallarını temizle (sistem ayarlarında
            // birikmesini önlemek için — işlevsel bir zorunluluk değil, düzen amaçlı)
            cleanupOldSoundChannels(manager, keepChannelId = channelId)
        }
    }

    private fun cleanupOldSoundChannels(manager: NotificationManager, keepChannelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.notificationChannels
            .filter { it.id.startsWith(CHANNEL_ID_SOUND_PREFIX) && it.id != keepChannelId }
            .forEach { manager.deleteNotificationChannel(it.id) }
    }

    // Kullanıcının ayarlar ekranında seçtiği zil sesini döndürür.
    // Seçim yapılmadıysa (boş string) sistem varsayılan bildirim sesine düşer.
    private fun getSelectedSoundUri(context: Context): Uri {
        val store = WaterDataStore(context)
        val savedUriString = runBlocking { store.reminderSoundUri.first() }
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