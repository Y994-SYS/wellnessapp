package com.alkanyazilim.wellnesapp.ui.workout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.alkanyazilim.wellnesapp.ui.util.rememberCurrentLocale
import java.util.Locale

private enum class RunGoalMode { STEPS, DURATION }

@Composable
fun RunScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    val runData by RunSessionState.data.collectAsState()

    // YENİ: Hedef türü — Adım veya Süre
    var goalMode by remember { mutableStateOf(RunGoalMode.STEPS) }
    var targetSteps by remember { mutableStateOf(2000) }
    var targetDurationSeconds by remember { mutableStateOf(900) } // 15 dk

    if (!hasPermission) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Koşu takibi için adım sensörü izni gerekiyor", modifier = Modifier.padding(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) }) {
                    Text("İzin ver")
                }
            }
        }
        return
    }

    if (!runData.isRunning) {
        TargetPickerScreen(
            modifier = modifier,
            goalMode = goalMode,
            onGoalModeChange = { goalMode = it },
            targetSteps = targetSteps,
            onTargetStepsChange = { targetSteps = it },
            targetDurationSeconds = targetDurationSeconds,
            onTargetDurationChange = { targetDurationSeconds = it },
            onStart = {
                val intent = Intent(context, RunTrackingService::class.java).apply {
                    action = RunTrackingService.ACTION_START
                    putExtra(
                        RunTrackingService.EXTRA_GOAL_TYPE,
                        if (goalMode == RunGoalMode.STEPS) RunGoalType.STEPS.name else RunGoalType.DURATION.name
                    )
                    putExtra(RunTrackingService.EXTRA_TARGET_STEPS, targetSteps)
                    putExtra(RunTrackingService.EXTRA_TARGET_DURATION_SECONDS, targetDurationSeconds)
                }
                ContextCompat.startForegroundService(context, intent)
            }
        )
    } else {
        ActiveRunScreen(
            modifier = modifier,
            goalType = runData.goalType,
            targetSteps = runData.targetSteps,
            targetDurationSeconds = runData.targetDurationSeconds,
            sessionSteps = runData.sessionSteps,
            elapsedSeconds = runData.elapsedSeconds,
            onStop = {
                val intent = Intent(context, RunTrackingService::class.java).apply {
                    action = RunTrackingService.ACTION_STOP
                }
                context.startService(intent)
            }
        )
    }
}

@Composable
private fun TargetPickerScreen(
    modifier: Modifier,
    goalMode: RunGoalMode,
    onGoalModeChange: (RunGoalMode) -> Unit,
    targetSteps: Int,
    onTargetStepsChange: (Int) -> Unit,
    targetDurationSeconds: Int,
    onTargetDurationChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    val stepPresets = listOf(1000, 2000, 3000, 5000, 8000)
    // YENİ: Süre ön ayarları (saniye cinsinden)
    val durationPresets = listOf(15 * 60, 30 * 60, 45 * 60, 60 * 60)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Koşu Hedefi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // YENİ: Adım / Süre mod seçici
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = goalMode == RunGoalMode.STEPS,
                    onClick = { onGoalModeChange(RunGoalMode.STEPS) },
                    label = { Text("Adım") }
                )
                FilterChip(
                    selected = goalMode == RunGoalMode.DURATION,
                    onClick = { onGoalModeChange(RunGoalMode.DURATION) },
                    label = { Text("Süre") }
                )
            }

            Spacer(Modifier.height(24.dp))

            if (goalMode == RunGoalMode.STEPS) {
                Text(text = "$targetSteps adım", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onTargetStepsChange((targetSteps - 500).coerceAtLeast(500)) }) {
                        Text("- 500")
                    }
                    OutlinedButton(onClick = { onTargetStepsChange(targetSteps + 500) }) {
                        Text("+ 500")
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Hızlı seçim", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    stepPresets.forEach { preset ->
                        FilterChip(
                            selected = preset == targetSteps,
                            onClick = { onTargetStepsChange(preset) },
                            label = { Text("$preset") }
                        )
                    }
                }
            } else {
                // YENİ: Süre seçici
                val minutes = targetDurationSeconds / 60
                Text(text = "$minutes dk", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {
                        onTargetDurationChange((targetDurationSeconds - 5 * 60).coerceAtLeast(5 * 60))
                    }) {
                        Text("- 5dk")
                    }
                    OutlinedButton(onClick = {
                        onTargetDurationChange(targetDurationSeconds + 5 * 60)
                    }) {
                        Text("+ 5dk")
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Hızlı seçim", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    durationPresets.forEach { preset ->
                        FilterChip(
                            selected = preset == targetDurationSeconds,
                            onClick = { onTargetDurationChange(preset) },
                            label = { Text("${preset / 60} dk") }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Süre dolduğunda sesli ve titreşimli bir bildirim alacaksın",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Başla", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ActiveRunScreen(
    modifier: Modifier,
    goalType: RunGoalType,
    targetSteps: Int,
    targetDurationSeconds: Int,
    sessionSteps: Int,
    elapsedSeconds: Int,
    onStop: () -> Unit
) {
    val distanceKm = sessionSteps * 0.000762
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val locale = rememberCurrentLocale()

    // YENİ: Hedef türüne göre ilerleme yüzdesi ve merkez metni değişir
    val progress = if (goalType == RunGoalType.DURATION) {
        (elapsedSeconds.toFloat() / targetDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        (sessionSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
    }

    val goalReached = if (goalType == RunGoalType.DURATION) {
        elapsedSeconds >= targetDurationSeconds
    } else {
        sessionSteps >= targetSteps
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 16.dp,
                    color = if (goalReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (goalType == RunGoalType.DURATION) {
                        val remaining = (targetDurationSeconds - elapsedSeconds).coerceAtLeast(0)
                        Text(
                            String.format(locale, "%02d:%02d", remaining / 60, remaining % 60),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("kalan süre", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("$sessionSteps", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Text("/ $targetSteps adım", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (goalReached) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "🎉 Hedefine ulaştın!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(locale, "%02d:%02d", minutes, seconds), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Süre", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(locale, "%.2f km", distanceKm), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Mesafe", style = MaterialTheme.typography.bodySmall)
                }
                if (goalType == RunGoalType.DURATION) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$sessionSteps", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Adım", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Uygulama arka planda da devam eder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Durdur", fontSize = 18.sp)
            }
        }
    }
}