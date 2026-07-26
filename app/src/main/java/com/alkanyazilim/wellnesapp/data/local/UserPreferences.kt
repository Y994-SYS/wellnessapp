package com.alkanyazilim.wellnesapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val STEP_GOAL_KEY = intPreferencesKey("step_goal")
        const val DEFAULT_STEP_GOAL = 10000
    }

    val stepGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STEP_GOAL_KEY] ?: DEFAULT_STEP_GOAL
    }

    suspend fun setStepGoal(goal: Int) {
        context.dataStore.edit { prefs ->
            prefs[STEP_GOAL_KEY] = goal
        }
    }
}