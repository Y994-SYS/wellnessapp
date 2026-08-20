@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.data.local.RunSessionEntity
import com.alkanyazilim.wellnesapp.data.repository.RunSessionRepository
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import com.alkanyazilim.wellnesapp.ui.util.rememberCurrentLocale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RunHistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { RunSessionRepository(AppDatabase.getInstance(context).runSessionDao()) }
    val coroutineScope = rememberCoroutineScope()

    val sessions by repository.allSessions.collectAsState(initial = emptyList())

    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    val selectionMode = selectedIds.isNotEmpty()

    fun toggleSelection(id: Int) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Scaffold(
        modifier = Modifier.background(AppColors.Background),
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectionMode) "${selectedIds.size} seçili" else "Koşu Geçmişi",
                        color = AppColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (selectionMode) selectedIds = emptySet() else onBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                repository.deleteSessions(selectedIds.toList())
                                selectedIds = emptySet()
                            }
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Seçilenleri sil",
                                tint = AppColors.ExerciseAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background,
                    titleContentColor = AppColors.TextPrimary,
                    navigationIconContentColor = AppColors.TextPrimary,
                    actionIconContentColor = AppColors.ExerciseAccent
                )
            )
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Henüz kayıtlı koşu yok.",
                    color = AppColors.TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    RunHistoryRow(
                        session = session,
                        isSelected = session.id in selectedIds,
                        selectionMode = selectionMode,
                        onClick = {
                            if (selectionMode) toggleSelection(session.id)
                        },
                        onLongClick = { toggleSelection(session.id) },
                        onDelete = {
                            coroutineScope.launch { repository.deleteSession(session.id) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RunHistoryRow(
    session: RunSessionEntity,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val locale = rememberCurrentLocale()
    val dateFormat = remember(locale) { SimpleDateFormat("d MMMM yyyy", Locale("tr")) }
    val timeFormat = remember(locale) { SimpleDateFormat("HH:mm", Locale("tr")) }

    val dateLabel = dateFormat.format(session.startTimeMillis)
    val startLabel = timeFormat.format(session.startTimeMillis)
    val endLabel = timeFormat.format(session.endTimeMillis)
    val distanceKm = session.steps * 0.000762
    val minutes = session.durationSeconds / 60
    val seconds = session.durationSeconds % 60
    val durationLabel = if (minutes > 0) {
        String.format(locale, "%d dk %02d sn", minutes, seconds)
    } else {
        String.format(locale, "%d sn", seconds)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                AppColors.HomeAccent.copy(alpha = 0.15f)
            else
                AppColors.Surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Üst satır: tarih + süre rozeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary
                )

                if (!selectionMode) {
                    DurationBadge(durationLabel = durationLabel)
                }
            }

            Spacer(Modifier.height(6.dp))

            // Saat aralığı, ikonla birlikte
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "$startLabel - $endLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Alt satır: adım ve mesafe, ikonlarla
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatChip(
                    icon = Icons.Filled.DirectionsWalk,
                    text = "${session.steps} / ${session.targetSteps} adım"
                )
                StatChip(
                    icon = Icons.Filled.Straighten,
                    text = String.format(locale, "%.2f km", distanceKm)
                )
            }
        }

        if (selectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.HomeAccent,
                        uncheckedColor = AppColors.TextSecondary
                    )
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Sil",
                        tint = AppColors.ExerciseAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationBadge(durationLabel: String) {
    Row(
        modifier = Modifier
            .background(
                color = AppColors.HomeAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Timer,
            contentDescription = null,
            tint = AppColors.HomeAccent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            durationLabel,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.HomeAccent
        )
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}