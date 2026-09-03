package com.alkanyazilim.wellnesapp.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alkanyazilim.wellnesapp.data.backup.BackupManager
import com.alkanyazilim.wellnesapp.data.local.AppSettingsDataStore
import com.alkanyazilim.wellnesapp.data.local.ThemeMode
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: AppSettingsDataStore,
    private val userPreferences: UserPreferences,
    private val waterStore: WaterDataStore,
    // YENİ
    private val backupManager: BackupManager
) : ViewModel() {

    val themeMode = settingsStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val userName = settingsStore.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userWeightKg = settingsStore.userWeightKg
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 70)
    val userHeightCm = settingsStore.userHeightCm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 170)
    val userAge = settingsStore.userAge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val stepGoal = userPreferences.stepGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_STEP_GOAL)

    val waterGoal = waterStore.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    val glassSize = waterStore.glassSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun saveProfile(name: String, weightKg: Int, heightCm: Int, age: Int) {
        viewModelScope.launch { settingsStore.setProfile(name, weightKg, heightCm, age) }
    }

    fun setStepGoal(goal: Int) {
        viewModelScope.launch { userPreferences.setStepGoal(goal) }
    }

    fun setWaterGoal(ml: Int) {
        viewModelScope.launch { waterStore.setDailyGoal(ml) }
    }

    fun setGlassSize(ml: Int) {
        viewModelScope.launch { waterStore.setGlassSize(ml) }
    }

    // ---- YENİ: Yedekle / Geri Yükle ----

    /** Tüm veriyi [uri]'ye JSON olarak yazar. Sonucu [onComplete] ile bildirir. */
    fun exportData(uri: Uri, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = runCatching { backupManager.exportToUri(uri) }
            onComplete(result)
        }
    }

    /**
     * [uri]'deki yedek dosyasını okuyup mevcut TÜM yerel veriyi bununla değiştirir.
     * Bu işlem yıkıcıdır (destructive) — UI tarafında çağırmadan önce kullanıcıdan
     * onay almalısın.
     */
    fun importData(uri: Uri, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = runCatching { backupManager.importFromUri(uri) }
            onComplete(result)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            return SettingsViewModel(
                AppSettingsDataStore(appContext),
                UserPreferences(appContext),
                WaterDataStore(appContext),
                BackupManager(appContext)
            ) as T
        }
    }
}