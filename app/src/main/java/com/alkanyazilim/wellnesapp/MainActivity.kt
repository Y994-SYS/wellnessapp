package com.alkanyazilim.wellnesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.alkanyazilim.wellnesapp.data.local.AppSettingsDataStore
import com.alkanyazilim.wellnesapp.data.local.ThemeMode
import com.alkanyazilim.wellnesapp.ui.AppNavigation
import com.alkanyazilim.wellnesapp.ui.theme.WellnesAppTheme
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestinationFromNotification = intent?.getStringExtra("navigate_to")

        setContent {
            val context = LocalContext.current
            val settingsStore = remember { AppSettingsDataStore(context) }
            val themeMode by settingsStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            // null: henüz DataStore'dan okunmadı, true/false: onboarding durumu belli
            val onboardingCompleted by settingsStore.onboardingCompleted.collectAsState(initial = null)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            WellnesAppTheme(darkTheme = darkTheme) {
                val completed = onboardingCompleted
                if (completed == null) {
                    // Onboarding durumu henüz okunmadı — kısa bir an boş ekran
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                } else {
                    val resolvedStart = startDestinationFromNotification
                        ?: if (completed) null else "onboarding"
                    AppNavigation(startDestination = resolvedStart)
                }
            }
        }
    }
}