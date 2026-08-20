package com.alkanyazilim.wellnesapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.waterDataStore by preferencesDataStore(name = "water_prefs")

class WaterDataStore(private val context: Context) {

    companion object {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val GLASS_SIZE_ML = intPreferencesKey("glass_size_ml")

        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_INTERVAL_MIN = intPreferencesKey("reminder_interval_minutes")
        val REMINDER_START_HOUR = intPreferencesKey("reminder_start_hour")
        val REMINDER_END_HOUR = intPreferencesKey("reminder_end_hour")
        val REMINDER_SOUND_ENABLED = booleanPreferencesKey("reminder_sound_enabled")
        val REMINDER_SOUND_URI = stringPreferencesKey("reminder_sound_uri")

        private const val CONSUMED_KEY_PREFIX = "consumed_"
        private fun consumedKeyFor(date: String) = intPreferencesKey("$CONSUMED_KEY_PREFIX$date")
    }

    val dailyGoal: Flow<Int> = context.waterDataStore.data.map { it[DAILY_GOAL_ML] ?: 2000 }
    val glassSize: Flow<Int> = context.waterDataStore.data.map { it[GLASS_SIZE_ML] ?: 250 }

    fun consumedForDate(date: String): Flow<Int> =
        context.waterDataStore.data.map { it[consumedKeyFor(date)] ?: 0 }

    // YENİ: Tüm günlere ait tüketim kayıtlarını (tarih -> ml) döndürür.
    // Haftalık/aylık/yıllık istatistikler bu veriden hesaplanır.
    val allConsumedEntries: Flow<Map<LocalDate, Int>> = context.waterDataStore.data.map { prefs ->
        prefs.asMap().entries
            .mapNotNull { (key, value) ->
                val name = key.name
                if (name.startsWith(CONSUMED_KEY_PREFIX) && value is Int) {
                    val dateString = name.removePrefix(CONSUMED_KEY_PREFIX)
                    runCatching { LocalDate.parse(dateString) }.getOrNull()?.let { it to value }
                } else {
                    null
                }
            }
            .toMap()
    }

    val reminderEnabled: Flow<Boolean> = context.waterDataStore.data.map { it[REMINDER_ENABLED] ?: false }
    val reminderIntervalMin: Flow<Int> = context.waterDataStore.data.map { it[REMINDER_INTERVAL_MIN] ?: 120 }
    val reminderStartHour: Flow<Int> = context.waterDataStore.data.map { it[REMINDER_START_HOUR] ?: 9 }
    val reminderEndHour: Flow<Int> = context.waterDataStore.data.map { it[REMINDER_END_HOUR] ?: 22 }
    val soundEnabled: Flow<Boolean> = context.waterDataStore.data.map { it[REMINDER_SOUND_ENABLED] ?: true }

    val reminderSoundUri: Flow<String> = context.waterDataStore.data.map { it[REMINDER_SOUND_URI] ?: "" }

    suspend fun setDailyGoal(ml: Int) = context.waterDataStore.edit { it[DAILY_GOAL_ML] = ml }
    suspend fun setGlassSize(ml: Int) = context.waterDataStore.edit { it[GLASS_SIZE_ML] = ml }

    suspend fun addWater(date: String = LocalDate.now().toString(), amountMl: Int) {
        context.waterDataStore.edit { prefs ->
            val key = consumedKeyFor(date)
            val current = prefs[key] ?: 0
            prefs[key] = (current + amountMl).coerceAtLeast(0)
        }
    }

    suspend fun setReminderSettings(
        enabled: Boolean,
        intervalMin: Int,
        startHour: Int,
        endHour: Int,
        soundEnabled: Boolean,
        soundUri: String = ""
    ) {
        context.waterDataStore.edit {
            it[REMINDER_ENABLED] = enabled
            it[REMINDER_INTERVAL_MIN] = intervalMin
            it[REMINDER_START_HOUR] = startHour
            it[REMINDER_END_HOUR] = endHour
            it[REMINDER_SOUND_ENABLED] = soundEnabled
            it[REMINDER_SOUND_URI] = soundUri
        }
    }
}