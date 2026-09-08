package com.alkanyazilim.wellnesapp.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.alkanyazilim.wellnesapp.data.local.AppSettingsDataStore
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.data.repository.HealthConnectManager
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import kotlinx.coroutines.launch

private const val TOTAL_STEPS = 6

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val settingsStore = remember { AppSettingsDataStore(context) }
    val userPreferences = remember { UserPreferences(context) }
    val waterStore = remember { WaterDataStore(context) }
    val healthConnectManager = remember { HealthConnectManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(0) }
    var stepGoal by remember { mutableIntStateOf(UserPreferences.DEFAULT_STEP_GOAL) }
    var waterGoal by remember { mutableIntStateOf(2000) }
    var healthConnectGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }

    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.requestPermissionsContract()
    ) { granted ->
        healthConnectGranted = granted.containsAll(healthConnectManager.permissions)
        currentStep++
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
        currentStep++
    }

    fun finish() {
        coroutineScope.launch {
            userPreferences.setStepGoal(stepGoal)
            waterStore.setDailyGoal(waterGoal)
            settingsStore.setOnboardingCompleted(true)
            onFinished()
        }
    }

    Scaffold(containerColor = AppColors.Background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            StepIndicator(current = currentStep, total = TOTAL_STEPS)
            Spacer(Modifier.height(32.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when (currentStep) {
                    0 -> IntroSlide(
                        emoji = "🚶",
                        accent = AppColors.StepsAccent,
                        title = "Adımlarını takip et",
                        description = "Günlük adım sayını, mesafeni ve yaktığın kaloriyi anlık takip et. Haftalık grafiklerle ilerlemeni gör."
                    )
                    1 -> IntroSlide(
                        emoji = "💧",
                        accent = AppColors.WaterAccent,
                        title = "Su tüketimini unutma",
                        description = "Günlük su hedefini belirle, hatırlatıcılarla düzenli su iç, haftalık/aylık istatistiklerini incele."
                    )
                    2 -> IntroSlide(
                        emoji = "🏆",
                        accent = AppColors.TasksAccent,
                        title = "Görevler, egzersiz ve rozetler",
                        description = "Alışkanlıklarını takip et, egzersiz kütüphanesinden sesli koçlukla antrenman yap, rozetler kazan."
                    )
                    3 -> PermissionSlide(
                        emoji = "❤️",
                        accent = AppColors.StepsAccent,
                        title = "Adım verilerine erişim",
                        description = "Health Connect üzerinden adım sayını okuyabilmemiz için izin vermen gerekiyor. Bu izni istediğin zaman iptal edebilirsin.",
                        buttonText = "İzin ver",
                        onGrant = {
                            if (healthConnectManager.isAvailable()) {
                                healthPermissionLauncher.launch(healthConnectManager.permissions)
                            } else {
                                currentStep++
                            }
                        },
                        onSkip = { currentStep++ }
                    )
                    4 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            PermissionSlide(
                                emoji = "🔔",
                                accent = AppColors.WaterAccent,
                                title = "Hatırlatıcı bildirimleri",
                                description = "Su içme ve görev hatırlatıcılarının çalışabilmesi için bildirim izni gerekiyor.",
                                buttonText = "İzin ver",
                                onGrant = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                onSkip = { currentStep++ }
                            )
                        } else {
                            // Android 13 altı ya da izin zaten verilmiş — otomatik geç
                            LaunchedEffect(Unit) { currentStep++ }
                        }
                    }
                    5 -> GoalSettingSlide(
                        stepGoal = stepGoal,
                        onStepGoalChange = { stepGoal = it },
                        waterGoal = waterGoal,
                        onWaterGoalChange = { waterGoal = it }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (currentStep in 0..2) {
                Button(
                    onClick = { currentStep++ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.HomeAccent)
                ) {
                    Text(if (currentStep == 2) "Devam Et" else "İleri", color = Color.White)
                }
            } else if (currentStep == 5) {
                Button(
                    onClick = { finish() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.HomeAccent)
                ) {
                    Text("Başlayalım", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 10.dp else 8.dp)
                    .background(
                        color = if (index == current) AppColors.HomeAccent else AppColors.TextSecondary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun IntroSlide(emoji: String, accent: Color, title: String, description: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(accent.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 44.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            description,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun PermissionSlide(
    emoji: String,
    accent: Color,
    title: String,
    description: String,
    buttonText: String,
    onGrant: () -> Unit,
    onSkip: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(accent.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 44.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            description,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = AppColors.TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(buttonText, color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSkip) {
            Text("Şimdi değil", color = AppColors.TextSecondary)
        }
    }
}

@Composable
private fun GoalSettingSlide(
    stepGoal: Int,
    onStepGoalChange: (Int) -> Unit,
    waterGoal: Int,
    onWaterGoalChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Hedeflerini belirle",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "İstediğin zaman Ayarlar'dan değiştirebilirsin",
            fontSize = 13.sp,
            color = AppColors.TextSecondary
        )
        Spacer(Modifier.height(32.dp))

        GoalStepper(
            label = "Günlük adım hedefi",
            value = stepGoal,
            step = 500,
            unit = "adım",
            accentColor = AppColors.StepsAccent,
            onChange = onStepGoalChange
        )
        Spacer(Modifier.height(24.dp))
        GoalStepper(
            label = "Günlük su hedefi",
            value = waterGoal,
            step = 250,
            unit = "ml",
            accentColor = AppColors.WaterAccent,
            onChange = onWaterGoalChange
        )
    }
}

@Composable
private fun GoalStepper(
    label: String,
    value: Int,
    step: Int,
    unit: String,
    accentColor: Color,
    onChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onChange((value - step).coerceAtLeast(step)) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) { Text("-") }
            Spacer(Modifier.width(20.dp))
            Text(
                "$value $unit",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.width(20.dp))
            OutlinedButton(
                onClick = { onChange(value + step) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) { Text("+") }
        }
    }
}