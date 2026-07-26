package com.alkanyazilim.wellnesapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.DirectionsRun

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Home : Screen("home", "Ana Sayfa", Icons.Filled.Home)
    object Steps : Screen("steps", "Adım", Icons.Filled.DirectionsWalk)
    object Water : Screen("water", "Su", Icons.Filled.WaterDrop)
    object Tasks : Screen("tasks", "Görevler", Icons.Filled.CheckCircle)
    object WaterReminderSettings : Screen("water_reminder_settings")
    object Workout : Screen("workout", "Egzersiz", Icons.Filled.DirectionsRun)
}

val bottomNavItems = listOf(Screen.Home, Screen.Steps, Screen.Water, Screen.Tasks,Screen.Workout)