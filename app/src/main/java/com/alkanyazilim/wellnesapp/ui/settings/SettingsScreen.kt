@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alkanyazilim.wellnesapp.data.local.ThemeMode
import com.alkanyazilim.wellnesapp.data.repository.BadgeUiState
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(context))

    val themeMode by viewModel.themeMode.collectAsState()
    val userWeight by viewModel.userWeightKg.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()
    val waterGoal by viewModel.waterGoal.collectAsState()
    val glassSize by viewModel.glassSize.collectAsState()
    val badgeStates by viewModel.badgeStates.collectAsState()

    var weightField by remember(userWeight) { mutableStateOf(userWeight.toString()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refreshBadges()
    }

    // İçe aktarma yıkıcı bir işlem: önce onay
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBadge by remember { mutableStateOf<BadgeUiState?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportData(uri) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Yedek başarıyla kaydedildi")
                    } else {
                        snackbarHostState.showSnackbar("Yedekleme başarısız oldu: ${result.exceptionOrNull()?.message ?: "bilinmeyen hata"}")
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingImportUri = uri
    }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Yedeği geri yükle?") },
            text = {
                Text(
                    "Bu işlem, cihazındaki mevcut tüm görev, koşu geçmişi, su kayıtları " +
                            "ve profil bilgilerinin ÜZERİNE yazacak. Bu işlem geri alınamaz. " +
                            "Devam etmek istiyor musun?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri!!
                    pendingImportUri = null
                    viewModel.importData(uri) { result ->
                        coroutineScope.launch {
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Veriler geri yüklendi")
                            } else {
                                snackbarHostState.showSnackbar("Geri yükleme başarısız oldu: ${result.exceptionOrNull()?.message ?: "bilinmeyen hata"}")
                            }
                        }
                    }
                }) {
                    Text("Evet, geri yükle", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text("Vazgeç") }
            }
        )
    }

    selectedBadge?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            title = { Text("${badge.definition.emoji} ${badge.definition.title}") },
            text = {
                val unlockedAt = badge.unlockedAt
                Column {
                    Text(badge.definition.description)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (badge.isUnlocked && unlockedAt != null) {
                            "✅ Kazanıldı: " + SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(Date(unlockedAt))
                        } else {
                            "🔒 Henüz kilitli"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) { Text("Tamam") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                    ThemeChip("Açık", themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
                    ThemeChip("Koyu", themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
                    ThemeChip("Sistem", themeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                }
            }

            // Profil kartı: sadece kilo (egzersiz kalori hesabı bunu kullanıyor)
            SettingsSection(title = "Kilo") {
                Text(
                    "Egzersiz kalorileri kilona göre hesaplanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightField,
                        onValueChange = { weightField = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Kilo (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.HomeAccent,
                            focusedLabelColor = AppColors.HomeAccent,
                            cursorColor = AppColors.HomeAccent
                        )
                    )
                    Button(
                        onClick = {
                            val kg = (weightField.toIntOrNull() ?: 70).coerceIn(30, 250)
                            weightField = kg.toString()
                            viewModel.saveWeight(kg)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Kilo kaydedildi") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.HomeAccent)
                    ) {
                        Text("Kaydet", color = Color.White)
                    }
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

            SettingsSection(title = "Rozetler") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(badgeStates) { badge ->
                        val badgeAlpha = if (badge.isUnlocked) 1.0f else 0.35f
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { selectedBadge = badge }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badge.definition.emoji,
                                    fontSize = 28.sp,
                                    modifier = Modifier.alpha(badgeAlpha)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = badge.definition.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = badgeAlpha),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "Yedekle ve Geri Yükle") {
                Text(
                    "Tüm görevlerin, koşu geçmişin, su kayıtların ve kilo bilgin tek bir " +
                            "dosyaya kaydedilir. Telefon değiştirirken ya da uygulamayı yeniden " +
                            "kurarken bu dosyayla verilerini geri getirebilirsin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale("tr")).format(Date())
                            exportLauncher.launch("wellnesapp_yedek_$timestamp.json")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.HomeAccent)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Dışa Aktar", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.HomeAccent)
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("İçe Aktar", maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.HomeAccent,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onChange((value - step).coerceAtLeast(step)) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) { Text("-") }
            Spacer(Modifier.width(16.dp))
            Text(
                "$value $unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(16.dp))
            OutlinedButton(
                onClick = { onChange(value + step) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) { Text("+") }
        }
    }
}