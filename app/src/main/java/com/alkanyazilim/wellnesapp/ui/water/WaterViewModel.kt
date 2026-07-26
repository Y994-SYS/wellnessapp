package com.alkanyazilim.wellnesapp.ui.water

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.utils.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WaterViewModel(
    private val context: Context,
    private val store: WaterDataStore
) : ViewModel() {

    private fun today() = LocalDate.now().toString()

    // Her 30 saniyede bir güncel tarihi yayınlar; tarih değişmediyse tekrar emit etmez (distinctUntilChanged)
    private val currentDateFlow: Flow<String> = flow {
        while (true) {
            emit(today())
            delay(30_000)
        }
    }.distinctUntilChanged()

    val dailyGoal = store.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    val glassSize = store.glassSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)

    // Artık sabit bir tarihe değil, currentDateFlow'a bağlı — tarih değişince otomatik yeniden okur
    val consumedToday = currentDateFlow
        .flatMapLatest { date -> store.consumedForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val reminderEnabled = store.reminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderIntervalMin = store.reminderIntervalMin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)

    val reminderStartHour = store.reminderStartHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9)

    val reminderEndHour = store.reminderEndHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)

    fun addGlass() = viewModelScope.launch { store.addWater(today(), glassSize.value) }
    fun removeGlass() = viewModelScope.launch { store.addWater(today(), -glassSize.value) }
    fun updateGoal(ml: Int) = viewModelScope.launch { store.setDailyGoal(ml) }
    fun updateGlassSize(ml: Int) = viewModelScope.launch { store.setGlassSize(ml) }

    fun saveReminderSettings(enabled: Boolean, intervalMin: Int, startHour: Int, endHour: Int) {
        viewModelScope.launch {
            store.setReminderSettings(enabled, intervalMin, startHour, endHour)
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