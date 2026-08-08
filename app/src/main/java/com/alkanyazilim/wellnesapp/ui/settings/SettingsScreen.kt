@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alkanyazilim.wellnesapp.data.local.ThemeMode
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(context))

    val themeMode by viewModel.themeMode.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userWeight by viewModel.userWeightKg.collectAsState()
    val userHeight by viewModel.userHeightCm.collectAsState()
    val userAge by viewModel.userAge.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()
    val waterGoal by viewModel.waterGoal.collectAsState()
    val glassSize by viewModel.glassSize.collectAsState()

    var nameField by remember(userName) { mutableStateOf(userName) }
    var weightField by remember(userWeight) { mutableStateOf(userWeight.toString()) }
    var heightField by remember(userHeight) { mutableStateOf(userHeight.toString()) }
    var ageField by remember(userAge) { mutableStateOf(userAge.toString()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.background(AppColors.Background),
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ayarlar",
                        color = AppColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background,
                    titleContentColor = AppColors.TextPrimary,
                    navigationIconContentColor = AppColors.TextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .background(AppColors.Background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "Tema") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        label = { Text("Açık", color = AppColors.TextPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.HomeAccent,
                            selectedLabelColor = Color.White,
                            containerColor = AppColors.Surface,
                            labelColor = AppColors.TextPrimary
                        )
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        label = { Text("Koyu", color = AppColors.TextPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.HomeAccent,
                            selectedLabelColor = Color.White,
                            containerColor = AppColors.Surface,
                            labelColor = AppColors.TextPrimary
                        )
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        label = { Text("Sistem", color = AppColors.TextPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.HomeAccent,
                            selectedLabelColor = Color.White,
                            containerColor = AppColors.Surface,
                            labelColor = AppColors.TextPrimary
                        )
                    )
                }
            }

            SettingsSection(title = "Profil") {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("İsim", color = AppColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.HomeAccent,
                        focusedLabelColor = AppColors.HomeAccent,
                        cursorColor = AppColors.HomeAccent,
                        unfocusedBorderColor = AppColors.TextSecondary.copy(alpha = 0.3f)
                    )
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightField,
                        onValueChange = { weightField = it.filter { c -> c.isDigit() } },
                        label = { Text("Kilo (kg)", color = AppColors.TextSecondary) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.HomeAccent,
                            focusedLabelColor = AppColors.HomeAccent,
                            cursorColor = AppColors.HomeAccent,
                            unfocusedBorderColor = AppColors.TextSecondary.copy(alpha = 0.3f)
                        )
                    )
                    OutlinedTextField(
                        value = heightField,
                        onValueChange = { heightField = it.filter { c -> c.isDigit() } },
                        label = { Text("Boy (cm)", color = AppColors.TextSecondary) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.HomeAccent,
                            focusedLabelColor = AppColors.HomeAccent,
                            cursorColor = AppColors.HomeAccent,
                            unfocusedBorderColor = AppColors.TextSecondary.copy(alpha = 0.3f)
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ageField,
                    onValueChange = { ageField = it.filter { c -> c.isDigit() } },
                    label = { Text("Yaş", color = AppColors.TextSecondary) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.5f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.HomeAccent,
                        focusedLabelColor = AppColors.HomeAccent,
                        cursorColor = AppColors.HomeAccent,
                        unfocusedBorderColor = AppColors.TextSecondary.copy(alpha = 0.3f)
                    )
                )

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.saveProfile(
                            name = nameField.trim(),
                            weightKg = weightField.toIntOrNull() ?: 70,
                            heightCm = heightField.toIntOrNull() ?: 170,
                            age = ageField.toIntOrNull() ?: 25
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Profil kaydedildi")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.HomeAccent
                    )
                ) {
                    Text("Profili Kaydet", color = Color.White)
                }
            }

            SettingsSection(title = "Hedefler") {
                GoalStepperRow(
                    label = "Günlük adım hedefi",
                    value = stepGoal,
                    step = 500,
                    unit = "adım",
                    accentColor = AppColors.StepsAccent,
                    onChange = { viewModel.setStepGoal(it) }
                )
                Spacer(Modifier.height(16.dp))
                GoalStepperRow(
                    label = "Günlük su hedefi",
                    value = waterGoal,
                    step = 250,
                    unit = "ml",
                    accentColor = AppColors.WaterAccent,
                    onChange = { viewModel.setWaterGoal(it) }
                )
                Spacer(Modifier.height(16.dp))
                GoalStepperRow(
                    label = "Bardak boyutu",
                    value = glassSize,
                    step = 50,
                    unit = "ml",
                    accentColor = AppColors.WaterAccent,
                    onChange = { viewModel.setGlassSize(it) }
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun GoalStepperRow(
    label: String,
    value: Int,
    step: Int,
    unit: String,
    accentColor: Color,
    onChange: (Int) -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onChange((value - step).coerceAtLeast(step)) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = accentColor
                )
            ) {
                Text("-")
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "$value $unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.width(16.dp))
            OutlinedButton(
                onClick = { onChange(value + step) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = accentColor
                )
            ) {
                Text("+")
            }
        }
    }
}