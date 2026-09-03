package com.alkanyazilim.wellnesapp.ui.steps

// test aider bağlantısı

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.model.DailySteps
import com.alkanyazilim.wellnesapp.data.repository.HealthConnectManager
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun StepsScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage ?: "",
                modifier = Modifier.padding(24.dp),
                color = AppColors.ExerciseAccent // Mercan/Pembe - hata mesajı için
            )
        }
        return
    }

    when {
        !healthConnectManager.isAvailable() -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Health Connect bu cihazda kullanılamıyor",
                    color = AppColors.TextSecondary
                )
            }
        }
        !permissionsGranted -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { permissionLauncher.launch(healthConnectManager.permissions) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.StepsAccent
                    )
                ) {
                    Text("Adım verilerine erişim izni ver", color = Color.White)
                }
            }
        }
        todaySteps == null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppColors.StepsAccent
                )
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

    // Locale için LocalConfiguration kullan
    val locale = LocalConfiguration.current.locale

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
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
            WeeklyStepsChart(history = history, goal = goal.toLong())
        }

        item {
            Text(
                text = "Geçmiş Günler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        )
    ) {
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
                Text(
                    text = "Bugün",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.TextPrimary
                )
                IconButton(onClick = onGoalClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Hedef ayarla",
                        tint = AppColors.TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 14.dp,
                    color = AppColors.StepsAccent,
                    trackColor = AppColors.StepsAccent.copy(alpha = 0.15f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$steps",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "/ $goal adım",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Mesafe",
                    value = String.format("%.2f km", distanceKm)
                )
                StatItem(
                    label = "Kalori",
                    value = String.format("%.0f kcal", calories)
                )
                StatItem(
                    label = "Hedef",
                    value = "${(progress * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun DailyStepsRow(day: DailySteps, onClick: () -> Unit) {
    val distanceKm = day.steps * 0.000762
    val calories = day.steps * 0.04

    // Locale için LocalConfiguration kullan
    val locale = LocalConfiguration.current.locale

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val dayLabel = day.date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
                val weekdayLabel = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                Text(
                    text = dayLabel,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = weekdayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.steps} adım",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = String.format("%.2f km · %.0f kcal", distanceKm, calories),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
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
    val accentColor = AppColors.StepsAccent

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "Günlük adım hedefi",
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "$selectedGoal adım",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { selectedGoal = (selectedGoal - 500).coerceAtLeast(1000) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = accentColor
                        )
                    ) {
                        Text("- 500")
                    }
                    OutlinedButton(
                        onClick = { selectedGoal = selectedGoal + 500 },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = accentColor
                        )
                    ) {
                        Text("+ 500")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Önerilen değerler:",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                LazyColumnPresetRow(
                    presets = presets,
                    selectedGoal = selectedGoal,
                    accentColor = accentColor,
                    onSelect = { selectedGoal = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedGoal) }) {
                Text("Tamam", color = accentColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = AppColors.TextSecondary)
            }
        }
    )
}

@Composable
private fun LazyColumnPresetRow(
    presets: List<Int>,
    selectedGoal: Int,
    accentColor: Color,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.take(4).forEach { preset ->
            FilterChip(
                selected = preset == selectedGoal,
                onClick = { onSelect(preset) },
                label = { Text("$preset") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Surface,
                    labelColor = AppColors.TextPrimary
                )
            )
        }
    }
}

@Composable
private fun WeeklyStepsChart(history: List<DailySteps>, goal: Long) {
    // history en yeniden en eskiye sıralı geliyor (readStepsForLastDays).
    // Son 7 günü alıp kronolojik sıraya (eskiden yeniye) çeviriyoruz.
    val lastWeek = history.take(7).sortedBy { it.date }
    if (lastWeek.isEmpty()) return

    val today = java.time.LocalDate.now()

    // Locale için LocalConfiguration kullan
    val locale = LocalConfiguration.current.locale

    val maxValue = (lastWeek.maxOf { it.steps }.coerceAtLeast(goal)).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "Bu Hafta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val barCount = lastWeek.size
                val spacingPx = 12.dp.toPx()
                val barWidth = (size.width - spacingPx * (barCount - 1)) / barCount
                val goalY = size.height * (1f - (goal.toFloat() / maxValue).coerceIn(0f, 1f))

                // Hedef çizgisi (kesikli)
                drawLine(
                    color = AppColors.StepsAccent.copy(alpha = 0.4f),
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )

                lastWeek.forEachIndexed { index, day ->
                    val barHeightFraction = (day.steps.toFloat() / maxValue).coerceIn(0.02f, 1f)
                    val barHeight = size.height * barHeightFraction
                    val x = index * (barWidth + spacingPx)
                    val isToday = day.date == today

                    drawRoundRect(
                        color = if (isToday) AppColors.StepsAccent else AppColors.StepsAccent.copy(alpha = 0.35f),
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                lastWeek.forEach { day ->
                    val isToday = day.date == today
                    val dayName = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) AppColors.StepsAccent else AppColors.TextSecondary
                    )
                }
            }
        }
    }
}
