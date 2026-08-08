@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alkanyazilim.wellnesapp.data.local.TaskCategory
import com.alkanyazilim.wellnesapp.data.model.TaskTemplate
import com.alkanyazilim.wellnesapp.data.model.taskTemplates
import com.alkanyazilim.wellnesapp.data.repository.TaskWithStatus
import com.alkanyazilim.wellnesapp.ui.theme.AppColors

private fun categoryLabel(category: TaskCategory): String = when (category) {
    TaskCategory.SAGLIK -> "Sağlık"
    TaskCategory.SPOR -> "Spor"
    TaskCategory.KISISEL -> "Kişisel"
}

private fun categoryColor(category: TaskCategory): Color = when (category) {
    TaskCategory.SAGLIK -> AppColors.TasksAccent  // Yeşil
    TaskCategory.SPOR -> AppColors.StepsAccent    // Turuncu
    TaskCategory.KISISEL -> AppColors.HomeAccent  // Mor
}

@Composable
fun TasksScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory(context))
    val tasks by viewModel.tasks.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<TaskWithStatus?>(null) }

    val groupedTasks = tasks
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sortedBy { it.isCompleted } }

    val orderedCategories = listOf(TaskCategory.SAGLIK, TaskCategory.SPOR, TaskCategory.KISISEL)

    Scaffold(
        modifier = Modifier.background(AppColors.Background),
        containerColor = AppColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AppColors.TasksAccent
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Görev ekle",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Henüz görev yok. + ile ekleyebilirsin.",
                    color = AppColors.TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                orderedCategories.forEach { category ->
                    val categoryTasks = groupedTasks[category].orEmpty()
                    if (categoryTasks.isNotEmpty()) {
                        item(key = "header_${category.name}") {
                            CategoryHeader(category = category, count = categoryTasks.size)
                        }
                        items(categoryTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onToggle = { viewModel.toggleCompletion(task.id, task.isCompleted) },
                                onDelete = { viewModel.deleteTask(task.id) },
                                onClick = { taskBeingEdited = task }
                            )
                        }
                        item(key = "spacer_${category.name}") {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TaskDialog(
            initialTask = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, isRecurring, icon ->
                viewModel.addTask(title, category, isRecurring, icon)
                showAddDialog = false
            }
        )
    }

    taskBeingEdited?.let { task ->
        TaskDialog(
            initialTask = task,
            onDismiss = { taskBeingEdited = null },
            onConfirm = { title, category, isRecurring, icon ->
                viewModel.updateTask(task.id, title, category, isRecurring, icon)
                taskBeingEdited = null
            }
        )
    }
}

@Composable
private fun CategoryHeader(category: TaskCategory, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(categoryColor(category), shape = CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${categoryLabel(category)} ($count)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
    }
}

@Composable
private fun TaskRow(
    task: TaskWithStatus,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (task.isCompleted)
                    categoryColor(task.category).copy(alpha = 0.08f)
                else
                    categoryColor(task.category).copy(alpha = 0.16f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(categoryColor(task.category).copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(task.icon, fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) AppColors.TextSecondary else AppColors.TextPrimary
                    )
                    if (task.isRecurring) {
                        Text(
                            text = "Her gün tekrarlar",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = categoryColor(task.category),
                        uncheckedColor = AppColors.TextSecondary
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Sil",
                        tint = AppColors.ExerciseAccent // Mercan/Pembe - silme işlemi için uygun
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDialog(
    initialTask: TaskWithStatus?,
    onDismiss: () -> Unit,
    onConfirm: (String, TaskCategory, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTask?.category ?: TaskCategory.KISISEL) }
    var isRecurring by remember { mutableStateOf(initialTask?.isRecurring ?: false) }
    var selectedIcon by remember { mutableStateOf(initialTask?.icon ?: "📝") }

    val isEditing = initialTask != null
    val accentColor = categoryColor(selectedCategory)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                if (isEditing) "Görevi Düzenle" else "Yeni Görev",
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                if (!isEditing) {
                    Text(
                        "Hazır Görevler",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(taskTemplates) { template ->
                            TemplateChip(template = template) {
                                title = template.title
                                selectedCategory = template.category
                                isRecurring = template.isRecurring
                                selectedIcon = template.icon
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(accentColor.copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedIcon, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Görev başlığı") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            cursorColor = accentColor,
                            focusedLabelColor = accentColor
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "İkon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(commonIcons) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (emoji == selectedIcon) accentColor.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Kategori",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskCategory.values().forEach { category ->
                        val categoryAccent = categoryColor(category)
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(categoryLabel(category)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryAccent,
                                selectedLabelColor = Color.White,
                                containerColor = AppColors.Surface,
                                labelColor = AppColors.TextPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Her gün tekrarlansın",
                        color = AppColors.TextPrimary
                    )
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = accentColor,
                            checkedThumbColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), selectedCategory, isRecurring, selectedIcon) },
                enabled = title.isNotBlank()
            ) {
                Text(
                    if (isEditing) "Kaydet" else "Ekle",
                    color = if (title.isNotBlank()) accentColor else AppColors.TextSecondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = AppColors.TextSecondary)
            }
        }
    )
}

@Composable
private fun TemplateChip(template: TaskTemplate, onClick: () -> Unit) {
    val accentColor = categoryColor(template.category)

    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(accentColor.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(template.icon, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = template.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = AppColors.TextSecondary
        )
    }
}

private val commonIcons = listOf(
    "📝", "💧", "💊", "😴", "🧘", "🦷", "🚶", "🏃", "🏋️", "🤸",
    "🚴", "📖", "📓", "🧹", "🛒", "🎯", "☀️", "🌙", "🍎", "💪"
)