package com.alkanyazilim.wellnesapp.ui.water

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.utils.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class WaterViewModel(
    private val context: Context,
    private val store: WaterDataStore
) : ViewModel() {

    private fun today() = LocalDate.now().toString()

    val dailyGoal = store.dailyGoal.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    val glassSize = store.glassSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)
    val consumedToday = store.consumedForDate(today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val reminderEnabled = store.reminderEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val reminderIntervalMin = store.reminderIntervalMin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)
    val reminderStartHour = store.reminderStartHour.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9)
    val reminderEndHour = store.reminderEndHour.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)
    val reminderSoundEnabled = store.soundEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val reminderSoundUri = store.reminderSoundUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // YENİ: Tüm günlük kayıtlar (istatistik hesaplamalarının temeli)
    private val allConsumedEntries = store.allConsumedEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // YENİ: Son 7 gün (bugün dahil) toplam tüketim
    val weeklyTotal = allConsumedEntries.map { entries ->
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        entries.filterKeys { it in weekStart..today }.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // YENİ: İçinde bulunulan takvim ayı toplam tüketim
    val monthlyTotal = allConsumedEntries.map { entries ->
        val today = LocalDate.now()
        entries.filterKeys { it.year == today.year && it.month == today.month }.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // YENİ: İçinde bulunulan takvim yılı toplam tüketim
    val yearlyTotal = allConsumedEntries.map { entries ->
        val today = LocalDate.now()
        entries.filterKeys { it.year == today.year }.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addGlass() = viewModelScope.launch { store.addWater(today(), glassSize.value) }
    fun removeGlass() = viewModelScope.launch { store.addWater(today(), -glassSize.value) }
    fun updateGoal(ml: Int) = viewModelScope.launch { store.setDailyGoal(ml) }
    fun updateGlassSize(ml: Int) = viewModelScope.launch { store.setGlassSize(ml) }

    fun saveReminderSettings(
        enabled: Boolean,
        intervalMin: Int,
        startHour: Int,
        endHour: Int,
        soundEnabled: Boolean,
        soundUri: String = ""
    ) {
        viewModelScope.launch {
            store.setReminderSettings(enabled, intervalMin, startHour, endHour, soundEnabled, soundUri)
            if (enabled) {
                AlarmScheduler.scheduleNext(context, intervalMin, startHour, endHour)
            } else {
                AlarmScheduler.cancel(context)
            }
        }
    }

    class Factory(
        private val context: Context,
        private val store: WaterDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WaterViewModel(context.applicationContext, store) as T
        }
    }
}