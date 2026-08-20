@file:OptIn(ExperimentalMaterial3Api::class)

package com.alkanyazilim.wellnesapp.ui.water

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    val soundUri by viewModel.reminderSoundUri.collectAsState()

    var localEnabled by remember(enabled) { mutableStateOf(enabled) }
    var localInterval by remember(interval) { mutableStateOf(interval) }
    var localStartHour by remember(startHour) { mutableStateOf(startHour) }
    var localEndHour by remember(endHour) { mutableStateOf(endHour) }
    var localSoundEnabled by remember(soundEnabled) { mutableStateOf(soundEnabled) }
    var localSoundUri by remember(soundUri) { mutableStateOf(soundUri) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            localSoundUri = uri?.toString() ?: ""
        }
    }

    fun openRingtonePicker() {
        val currentUri = if (localSoundUri.isNotBlank()) {
            Uri.parse(localSoundUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Bildirim Sesi Seç")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
        }
        ringtonePickerLauncher.launch(intent)
    }

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

            if (localSoundEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { openRingtonePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentLabel = if (localSoundUri.isBlank()) {
                        "Varsayılan"
                    } else {
                        RingtoneManager.getRingtone(context, Uri.parse(localSoundUri))
                            ?.getTitle(context) ?: "Seçili ses"
                    }
                    Text("Zil Sesi: $currentLabel")
                }
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
                        soundEnabled = localSoundEnabled,
                        soundUri = localSoundUri
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