@file:OptIn(ExperimentalMaterial3Api::class)

package com.alkanyazilim.wellnesapp.ui.water

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore

private data class IntervalOption(val label: String, val minutes: Int)

private val intervalOptions = listOf(
    IntervalOption("30 dakika", 30),
    IntervalOption("1 saat", 60),
    IntervalOption("1.5 saat", 90),
    IntervalOption("2 saat", 120),
    IntervalOption("3 saat", 180),
    IntervalOption("4 saat", 240)
)

@Composable
fun WaterReminderSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { WaterDataStore(context) }
    val viewModel: WaterViewModel = viewModel(factory = WaterViewModel.Factory(context, store))

    val enabled by viewModel.reminderEnabled.collectAsState()
    val interval by viewModel.reminderIntervalMin.collectAsState()
    val startHour by viewModel.reminderStartHour.collectAsState()
    val endHour by viewModel.reminderEndHour.collectAsState()
    val soundEnabled by viewModel.reminderSoundEnabled.collectAsState()

    var localEnabled by remember(enabled) { mutableStateOf(enabled) }
    var localInterval by remember(interval) { mutableStateOf(interval) }
    var localStartHour by remember(startHour) { mutableStateOf(startHour) }
    var localEndHour by remember(endHour) { mutableStateOf(endHour) }
    var localSoundEnabled by remember(soundEnabled) { mutableStateOf(soundEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Su Hatırlatıcı Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hatırlatıcı", style = MaterialTheme.typography.titleMedium)
                Switch(checked = localEnabled, onCheckedChange = { localEnabled = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bildirim Sesi", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Açıksa alarm gibi çalar, ses tuşuyla susturabilirsin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = localSoundEnabled, onCheckedChange = { localSoundEnabled = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Sıklık", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            IntervalDropdown(selectedMinutes = localInterval, onSelected = { localInterval = it })

            Spacer(modifier = Modifier.height(24.dp))

            Text("Aktif Saat Aralığı", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HourPicker(
                    label = "Başlangıç",
                    hour = localStartHour,
                    onHourChange = { localStartHour = it },
                    modifier = Modifier.weight(1f)
                )
                HourPicker(
                    label = "Bitiş",
                    hour = localEndHour,
                    onHourChange = { localEndHour = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveReminderSettings(
                        enabled = localEnabled,
                        intervalMin = localInterval,
                        startHour = localStartHour,
                        endHour = localEndHour,
                        soundEnabled = localSoundEnabled
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }
    }
}

@Composable
private fun IntervalDropdown(selectedMinutes: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = intervalOptions.firstOrNull { it.minutes == selectedMinutes }?.label
        ?: "$selectedMinutes dakika"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ne sıklıkla hatırlatılsın") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            intervalOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onSelected(option.minutes); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun HourPicker(label: String, hour: Int, onHourChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = String.format("%02d:00", hour),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (0..23).forEach { h ->
                DropdownMenuItem(
                    text = { Text(String.format("%02d:00", h)) },
                    onClick = { onHourChange(h); expanded = false }
                )
            }
        }
    }
}