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
import com.alkanyazilim.wellnesapp.data.repository.BadgeRepository
import com.alkanyazilim.wellnesapp.data.repository.BadgeUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: AppSettingsDataStore,
    private val userPreferences: UserPreferences,
    private val waterStore: WaterDataStore,
    private val backupManager: BackupManager,
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    val themeMode = settingsStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val userWeightKg = settingsStore.userWeightKg
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 70)

    val stepGoal = userPreferences.stepGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_STEP_GOAL)

    val waterGoal = waterStore.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    val glassSize = waterStore.glassSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)

    val badgeStates: StateFlow<List<BadgeUiState>> = badgeRepository.badgeStates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshBadges() {
        viewModelScope.launch { badgeRepository.refreshBadges() }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    /**
     * Sadece kilo kaydedilir. İsim/boy/yaş UI'dan kalktı ama DataStore imzası
     * değişmesin diye mevcut kayıtlı değerler .first() ile okunup aynen geri yazılır.
     * (stateIn değeri kullanılmaz: abone yokken varsayılan değeri verir ve eski veriyi ezerdi.)
     */
    fun saveWeight(weightKg: Int) {
        viewModelScope.launch {
            val name = settingsStore.userName.first()
            val height = settingsStore.userHeightCm.first()
            val age = settingsStore.userAge.first()
            settingsStore.setProfile(name, weightKg, height, age)
        }
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

    // ---- Yedekle / Geri Yükle ----

    /** Tüm veriyi [uri]'ye JSON olarak yazar. Sonucu [onComplete] ile bildirir. */
    fun exportData(uri: Uri, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = runCatching { backupManager.exportToUri(uri) }
            onComplete(result)
        }
    }

    /**
     * [uri]'deki yedek dosyasını okuyup mevcut TÜM yerel veriyi bununla değiştirir.
     * Yıkıcı işlem: UI tarafında çağırmadan önce kullanıcıdan onay alınmalı.
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
            val waterDataStore = WaterDataStore(appContext)
            val badgeRepository = BadgeRepository.create(appContext)

            return SettingsViewModel(
                AppSettingsDataStore(appContext),
                UserPreferences(appContext),
                waterDataStore,
                BackupManager(appContext),
                badgeRepository
            ) as T
        }
    }
}