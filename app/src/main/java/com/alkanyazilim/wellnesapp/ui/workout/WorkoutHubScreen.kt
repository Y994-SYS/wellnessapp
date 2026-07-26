package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

private enum class ExerciseCategory(val label: String, val accent: Color) {
    KOSU("Koşu", Color(0xFF7E57C2)),
    KARDIYO("Kardiyo", Color(0xFFFF7043)),
    GUC("Güç", Color(0xFFE53935)),
    ESNEME("Esneme", Color(0xFF26A69A))
}

@Composable
fun WorkoutHubScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val categories = ExerciseCategory.values()

    Column(modifier = modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                    .background(Color(0xFFF0F0F5), shape = RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) category.accent else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.label,
                            color = if (selected) Color.White else Color.Gray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (categories[selectedTab] == ExerciseCategory.KOSU) {
                IconButton(
                    onClick = { navController.navigate("run_history") },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Filled.History, contentDescription = "Koşu geçmişi")
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (categories[selectedTab]) {
                ExerciseCategory.KOSU -> RunScreen()
                ExerciseCategory.KARDIYO -> CustomExerciseScreen(categoryLabel = "Kardiyo")
                ExerciseCategory.GUC -> CustomExerciseScreen(categoryLabel = "Güç")
                ExerciseCategory.ESNEME -> CustomExerciseScreen(categoryLabel = "Esneme")
            }
        }
    }
}