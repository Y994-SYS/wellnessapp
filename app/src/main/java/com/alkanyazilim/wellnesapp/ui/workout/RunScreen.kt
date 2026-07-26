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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

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
    var targetSteps by remember { mutableStateOf(2000) }

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
            targetSteps = targetSteps,
            onTargetChange = { targetSteps = it },
            onStart = {
                val intent = Intent(context, RunTrackingService::class.java).apply {
                    action = RunTrackingService.ACTION_START
                    putExtra(RunTrackingService.EXTRA_TARGET_STEPS, targetSteps)
                }
                ContextCompat.startForegroundService(context, intent)
            }
        )
    } else {
        ActiveRunScreen(
            modifier = modifier,
            targetSteps = runData.targetSteps,
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
    targetSteps: Int,
    onTargetChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    val presets = listOf(1000, 2000, 3000, 5000, 8000)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Koşu Hedefi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            Text(text = "$targetSteps adım", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onTargetChange((targetSteps - 500).coerceAtLeast(500)) }) {
                    Text("- 500")
                }
                OutlinedButton(onClick = { onTargetChange(targetSteps + 500) }) {
                    Text("+ 500")
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Hızlı seçim", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    FilterChip(
                        selected = preset == targetSteps,
                        onClick = { onTargetChange(preset) },
                        label = { Text("$preset") }
                    )
                }
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
    targetSteps: Int,
    sessionSteps: Int,
    elapsedSeconds: Int,
    onStop: () -> Unit
) {
    val distanceKm = sessionSteps * 0.000762
    val progress = (sessionSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 16.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$sessionSteps", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("/ $targetSteps adım", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Süre", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(Locale.getDefault(), "%.2f km", distanceKm), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Mesafe", style = MaterialTheme.typography.bodySmall)
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