package com.alkanyazilim.wellnesapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class AppSettingsDataStore(private val context: Context) {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_WEIGHT_KG = intPreferencesKey("user_weight_kg")
        private val USER_HEIGHT_CM = intPreferencesKey("user_height_cm")
        private val USER_AGE = intPreferencesKey("user_age")
    }

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }

    val userName: Flow<String> = context.settingsDataStore.data.map { it[USER_NAME] ?: "" }
    val userWeightKg: Flow<Int> = context.settingsDataStore.data.map { it[USER_WEIGHT_KG] ?: 70 }
    val userHeightCm: Flow<Int> = context.settingsDataStore.data.map { it[USER_HEIGHT_CM] ?: 170 }
    val userAge: Flow<Int> = context.settingsDataStore.data.map { it[USER_AGE] ?: 25 }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setProfile(name: String, weightKg: Int, heightCm: Int, age: Int) {
        context.settingsDataStore.edit {
            it[USER_NAME] = name
            it[USER_WEIGHT_KG] = weightKg
            it[USER_HEIGHT_CM] = heightCm
            it[USER_AGE] = age
        }
    }
}