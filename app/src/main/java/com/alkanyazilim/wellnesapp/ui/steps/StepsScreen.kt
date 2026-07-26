package com.alkanyazilim.wellnesapp.ui.steps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.model.DailySteps
import com.alkanyazilim.wellnesapp.data.repository.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StepsScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val userPreferences = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var permissionsGranted by remember { mutableStateOf(false) }
    var todaySteps by remember { mutableStateOf<Long?>(null) }
    var history by remember { mutableStateOf<List<DailySteps>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val stepGoal by userPreferences.stepGoal.collectAsState(initial = UserPreferences.DEFAULT_STEP_GOAL)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.requestPermissionsContract()
    ) { granted ->
        permissionsGranted = granted.containsAll(healthConnectManager.permissions)
    }

    LaunchedEffect(Unit) {
        try {
            permissionsGranted = healthConnectManager.hasAllPermissions()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = "hasAllPermissions hatası: ${e::class.simpleName} - ${e.message}"
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            try {
                todaySteps = healthConnectManager.readTodaySteps()
                history = healthConnectManager.readStepsForLastDays(14)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = "Veri okuma hatası: ${e::class.simpleName} - ${e.message}"
            }
        }
    }

    if (showGoalDialog) {
        GoalPickerDialog(
            currentGoal = stepGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                coroutineScope.launch { userPreferences.setStepGoal(newGoal) }
                showGoalDialog = false
            }
        )
    }

    if (errorMessage != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorMessage ?: "",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    when {
        !healthConnectManager.isAvailable() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Health Connect bu cihazda kullanılamıyor")
            }
        }
        !permissionsGranted -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { permissionLauncher.launch(healthConnectManager.permissions) }) {
                    Text("Adım verilerine erişim izni ver")
                }
            }
        }
        todaySteps == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            StepsContent(
                modifier = modifier,
                todaySteps = todaySteps!!,
                goal = stepGoal,
                history = history,
                onDayClick = { day -> navController.navigate("steps_detail/${day.date}") },
                onGoalClick = { showGoalDialog = true }
            )
        }
    }
}

@Composable
private fun StepsContent(
    modifier: Modifier,
    todaySteps: Long,
    goal: Int,
    history: List<DailySteps>,
    onDayClick: (DailySteps) -> Unit,
    onGoalClick: () -> Unit
) {
    val distanceKm = todaySteps * 0.000762
    val calories = todaySteps * 0.04

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            TodayStepsCard(
                steps = todaySteps,
                goal = goal.toLong(),
                distanceKm = distanceKm,
                calories = calories,
                onGoalClick = onGoalClick
            )
        }
        item {
            Text(
                text = "Geçmiş Günler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(history) { day ->
            DailyStepsRow(day = day, onClick = { onDayClick(day) })
        }
    }
}

@Composable
private fun TodayStepsCard(
    steps: Long,
    goal: Long,
    distanceKm: Double,
    calories: Double,
    onGoalClick: () -> Unit
) {
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(24.dp))
                Text("Bugün", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onGoalClick) {
                    Icon(Icons.Filled.Settings, contentDescription = "Hedef ayarla")
                }
            }
            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 14.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$steps", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(text = "/ $goal adım", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Mesafe", String.format(Locale.getDefault(), "%.2f km", distanceKm))
                StatItem("Kalori", String.format(Locale.getDefault(), "%.0f kcal", calories))
                StatItem("Hedef", "%${(progress * 100).toInt()}")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DailyStepsRow(day: DailySteps, onClick: () -> Unit) {
    val distanceKm = day.steps * 0.000762
    val calories = day.steps * 0.04
    val dayLabel = day.date.format(DateTimeFormatter.ofPattern("d MMMM", Locale("tr")))
    val weekdayLabel = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("tr"))

    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dayLabel, fontWeight = FontWeight.Bold)
                Text(weekdayLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${day.steps} adım", fontWeight = FontWeight.Bold)
                Text(
                    String.format(Locale.getDefault(), "%.2f km · %.0f kcal", distanceKm, calories),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GoalPickerDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedGoal by remember { mutableStateOf(currentGoal) }
    val presets = listOf(6000, 6500, 7000, 7500, 8000, 9000, 10000, 12000, 15000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Günlük adım hedefi") },
        text = {
            Column {
                Text(
                    text = "$selectedGoal adım",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { selectedGoal = (selectedGoal - 500).coerceAtLeast(1000) }) {
                        Text("- 500")
                    }
                    OutlinedButton(onClick = { selectedGoal = selectedGoal + 500 }) {
                        Text("+ 500")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Önerilen değerler:", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                LazyColumnPresetRow(presets = presets, selectedGoal = selectedGoal) {
                    selectedGoal = it
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedGoal) }) { Text("Tamam") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
private fun LazyColumnPresetRow(presets: List<Int>, selectedGoal: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.take(4).forEach { preset ->
            FilterChip(
                selected = preset == selectedGoal,
                onClick = { onSelect(preset) },
                label = { Text("$preset") }
            )
        }
    }
}