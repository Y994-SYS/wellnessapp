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
import kotlinx.coroutines.launch

private val SettingsCardTint = Color(0xFFEEE6FA)

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
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "Tema") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        label = { Text("Açık") }
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        label = { Text("Koyu") }
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        label = { Text("Sistem") }
                    )
                }
            }

            SettingsSection(title = "Profil") {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("İsim") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightField,
                        onValueChange = { weightField = it.filter { c -> c.isDigit() } },
                        label = { Text("Kilo (kg)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightField,
                        onValueChange = { heightField = it.filter { c -> c.isDigit() } },
                        label = { Text("Boy (cm)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ageField,
                    onValueChange = { ageField = it.filter { c -> c.isDigit() } },
                    label = { Text("Yaş") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.5f)
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
                    }
                ) {
                    Text("Profili Kaydet")
                }
            }

            SettingsSection(title = "Hedefler") {
                GoalStepperRow(
                    label = "Günlük adım hedefi",
                    value = stepGoal,
                    step = 500,
                    unit = "adım",
                    onChange = { viewModel.setStepGoal(it) }
                )
                Spacer(Modifier.height(16.dp))
                GoalStepperRow(
                    label = "Günlük su hedefi",
                    value = waterGoal,
                    step = 250,
                    unit = "ml",
                    onChange = { viewModel.setWaterGoal(it) }
                )
                Spacer(Modifier.height(16.dp))
                GoalStepperRow(
                    label = "Bardak boyutu",
                    value = glassSize,
                    step = 50,
                    unit = "ml",
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
        colors = CardDefaults.cardColors(containerColor = SettingsCardTint)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    onChange: (Int) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onChange((value - step).coerceAtLeast(step)) }) {
                Text("-")
            }
            Spacer(Modifier.width(16.dp))
            Text("$value $unit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            OutlinedButton(onClick = { onChange(value + step) }) {
                Text("+")
            }
        }
    }
}