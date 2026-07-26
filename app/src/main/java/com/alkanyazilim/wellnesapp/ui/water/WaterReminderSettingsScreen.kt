@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.water

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import androidx.compose.material.icons.filled.ArrowBack

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

    var localEnabled by remember(enabled) { mutableStateOf(enabled) }
    var localInterval by remember(interval) { mutableStateOf(interval) }
    var localStartHour by remember(startHour) { mutableStateOf(startHour) }
    var localEndHour by remember(endHour) { mutableStateOf(endHour) }

    var showExactAlarmDialog by remember { mutableStateOf(false) }

    fun performSave() {
        viewModel.saveReminderSettings(
            enabled = localEnabled,
            intervalMin = localInterval,
            startHour = localStartHour,
            endHour = localEndHour
        )
        onBack()
    }

    fun checkExactAlarmAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmDialog = true
                return
            }
        }
        performSave()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Kullanıcı izni verse de vermese de devam ediyoruz (bildirim izni reddedilirse
        // sistem bildirimi göstermez ama uygulama çökmez, alarm yine kurulur)
        checkExactAlarmAndSave()
    }

    fun onSaveClicked() {
        if (!localEnabled) {
            performSave()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        checkExactAlarmAndSave()
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { Text("Tam zamanlı alarm izni gerekli") },
            text = {
                Text("Hatırlatıcıların tam zamanında gelmesi için sistem ayarlarından \"Alarmlar ve hatırlatıcılar\" iznini açman gerekiyor.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Ayarlara Git")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    performSave()
                }) {
                    Text("Yine de Kaydet")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Su Hatırlatıcı Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
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
                Switch(
                    checked = localEnabled,
                    onCheckedChange = { localEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Sıklık", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            IntervalDropdown(
                selectedMinutes = localInterval,
                onSelected = { localInterval = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Aktif Saat Aralığı", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                onClick = { onSaveClicked() },
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ne sıklıkla hatırlatılsın") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            intervalOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HourPicker(
    label: String,
    hour: Int,
    onHourChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = String.format("%02d:00", hour),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (0..23).forEach { h ->
                DropdownMenuItem(
                    text = { Text(String.format("%02d:00", h)) },
                    onClick = {
                        onHourChange(h)
                        expanded = false
                    }
                )
            }
        }
    }
}