@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.data.local.RunSessionEntity
import com.alkanyazilim.wellnesapp.data.repository.RunSessionRepository
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("tr")) }

    val dateLabel = dateFormat.format(session.startTimeMillis)
    val startLabel = timeFormat.format(session.startTimeMillis)
    val endLabel = timeFormat.format(session.endTimeMillis)
    val distanceKm = session.steps * 0.000762
    val minutes = session.durationSeconds / 60
    val seconds = session.durationSeconds % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                AppColors.HomeAccent.copy(alpha = 0.15f)
            else
                AppColors.Surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dateLabel,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    "$startLabel - $endLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${session.steps} / ${session.targetSteps} adım · %.2f km · %d:%02d".format(distanceKm, minutes, seconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }

            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.HomeAccent,
                        uncheckedColor = AppColors.TextSecondary
                    )
                )
            } else {
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