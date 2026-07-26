package com.alkanyazilim.wellnesapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.data.repository.HealthConnectManager
import com.alkanyazilim.wellnesapp.ui.Screen
import com.alkanyazilim.wellnesapp.ui.tasks.TasksViewModel
import com.alkanyazilim.wellnesapp.ui.water.WaterViewModel
import java.time.LocalTime

private val StepsColor = Color(0xFFFF7043)
private val WaterColor = Color(0xFF0288D1)
private val TasksColor = Color(0xFF43A047)

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    // --- Adım verisi ---
    val healthConnectManager = remember { HealthConnectManager(context) }
    val userPreferences = remember { UserPreferences(context) }
    val stepGoal by userPreferences.stepGoal.collectAsState(initial = UserPreferences.DEFAULT_STEP_GOAL)
    var todaySteps by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        try {
            if (healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()) {
                todaySteps = healthConnectManager.readTodaySteps()
            }
        } catch (e: Exception) {
            todaySteps = null
        }
    }

    // --- Su verisi ---
    val waterStore = remember { WaterDataStore(context) }
    val waterViewModel: WaterViewModel = viewModel(factory = WaterViewModel.Factory(context, waterStore))
    val waterGoal by waterViewModel.dailyGoal.collectAsState()
    val waterConsumed by waterViewModel.consumedToday.collectAsState()

    // --- Görev verisi ---
    val tasksViewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory(context))
    val tasks by tasksViewModel.tasks.collectAsState()
    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size

    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 12 -> "Günaydın"
            hour < 18 -> "İyi günler"
            else -> "İyi akşamlar"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Bugün formundasın 💪",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        SummaryCard(
            icon = Icons.Filled.DirectionsWalk,
            color = StepsColor,
            title = "Adım",
            currentText = todaySteps?.toString() ?: "--",
            goalText = "/ $stepGoal adım",
            progress = if (todaySteps != null) (todaySteps!!.toFloat() / stepGoal).coerceIn(0f, 1f) else 0f,
            onClick = { navController.navigate(Screen.Steps.route) }
        )

        Spacer(Modifier.height(16.dp))

        SummaryCard(
            icon = Icons.Filled.WaterDrop,
            color = WaterColor,
            title = "Su",
            currentText = "$waterConsumed ml",
            goalText = "/ $waterGoal ml",
            progress = (waterConsumed.toFloat() / waterGoal).coerceIn(0f, 1f),
            onClick = { navController.navigate(Screen.Water.route) },
            trailingAction = {
                FilledIconButton(
                    onClick = { waterViewModel.addGlass() },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = WaterColor)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Bir bardak ekle")
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        SummaryCard(
            icon = Icons.Filled.CheckCircle,
            color = TasksColor,
            title = "Görevler",
            currentText = "$completedTasks",
            goalText = "/ $totalTasks tamamlandı",
            progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f,
            onClick = { navController.navigate(Screen.Tasks.route) }
        )
    }
}

@Composable
private fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    currentText: String,
    goalText: String,
    progress: Float,
    onClick: () -> Unit,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        currentText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        " $goalText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f)
                )
            }

            trailingAction?.let {
                Spacer(Modifier.width(12.dp))
                it()
            }
        }
    }
}