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

private fun categoryLabel(category: TaskCategory): String = when (category) {
    TaskCategory.SAGLIK -> "Sağlık"
    TaskCategory.SPOR -> "Spor"
    TaskCategory.KISISEL -> "Kişisel"
}

private fun categoryColor(category: TaskCategory): Color = when (category) {
    TaskCategory.SAGLIK -> Color(0xFF4CAF50)
    TaskCategory.SPOR -> Color(0xFFFF9800)
    TaskCategory.KISISEL -> Color(0xFF7E57C2)
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Görev ekle")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz görev yok. + ile ekleyebilirsin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize(),
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
            fontWeight = FontWeight.Bold
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
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surface
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
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (task.isRecurring) {
                        Text(
                            text = "Her gün tekrarlar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Görevi Düzenle" else "Yeni Görev") },
        text = {
            Column {
                if (!isEditing) {
                    Text("Hazır Görevler", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
                            .background(categoryColor(selectedCategory).copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedIcon, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Görev başlığı") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text("İkon", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(commonIcons) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (emoji == selectedIcon) categoryColor(selectedCategory).copy(alpha = 0.3f) else Color.Transparent,
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
                Text("Kategori", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskCategory.values().forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(categoryLabel(category)) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Her gün tekrarlansın")
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), selectedCategory, isRecurring, selectedIcon) },
                enabled = title.isNotBlank()
            ) {
                Text(if (isEditing) "Kaydet" else "Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
private fun TemplateChip(template: TaskTemplate, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(categoryColor(template.category).copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(template.icon, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = template.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private val commonIcons = listOf(
    "📝", "💧", "💊", "😴", "🧘", "🦷", "🚶", "🏃", "🏋️", "🤸",
    "🚴", "📖", "📓", "🧹", "🛒", "🎯", "☀️", "🌙", "🍎", "💪"
)