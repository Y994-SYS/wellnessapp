@file:OptIn(ExperimentalMaterial3Api::class)
package com.alkanyazilim.wellnesapp.ui.water

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import com.alkanyazilim.wellnesapp.ui.Screen
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import kotlin.math.ceil
import androidx.compose.foundation.background
@Composable
fun WaterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val store = remember(context) { WaterDataStore(context) }
    val viewModel: WaterViewModel = viewModel(factory = WaterViewModel.Factory(context, store))

    val goal by viewModel.dailyGoal.collectAsState()
    val consumed by viewModel.consumedToday.collectAsState()
    val glass by viewModel.glassSize.collectAsState()
    val targetProgress = (consumed.toFloat() / goal).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500),
        label = "waterProgress"
    )

    val totalGlasses = ceil(goal.toFloat() / glass).toInt().coerceAtLeast(1)
    val filledGlasses = (consumed / glass).coerceIn(0, totalGlasses)

    Scaffold(
        modifier = Modifier.background(AppColors.Background),
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.WaterDrop,
                            contentDescription = null,
                            tint = AppColors.WaterAccent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Su Takibi",
                            color = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.WaterReminderSettings.route)
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Hatırlatıcı Ayarları",
                            tint = AppColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background,
                    titleContentColor = AppColors.TextPrimary,
                    actionIconContentColor = AppColors.TextSecondary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .background(AppColors.Background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WaterCircularGauge(
                        progress = animatedProgress,
                        consumed = consumed,
                        goal = goal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "$filledGlasses / $totalGlasses bardak içtin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.WaterAccent
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .heightIn(max = 220.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(totalGlasses) { index ->
                            val isFilled = index < filledGlasses
                            AnimatedGlassIcon(isFilled = isFilled)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.removeGlass() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.WaterAccent
                    )
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt")
                }
                Text(
                    "$glass ml / bardak",
                    color = AppColors.TextPrimary
                )
                Button(
                    onClick = { viewModel.addGlass() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.WaterAccent
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ekle")
                }
            }
        }
    }
}

@Composable
private fun AnimatedGlassIcon(isFilled: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glassScale"
    )
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = if (isFilled) AppColors.WaterAccent else AppColors.TextSecondary,
        animationSpec = tween(durationMillis = 300),
        label = "glassColor"
    )

    Icon(
        imageVector = if (isFilled) Icons.Filled.LocalDrink else Icons.Outlined.LocalDrink,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}

@Composable
private fun WaterCircularGauge(progress: Float, consumed: Int, goal: Int) {
    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val waterColor = AppColors.WaterAccent

            // Track (arka plan) çizimi
            drawArc(
                color = waterColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress çizimi
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        waterColor.copy(alpha = 0.7f),
                        waterColor
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = AppColors.WaterAccent,
                modifier = Modifier.size(28.dp)
            )
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                "$consumed / $goal ml",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
    }
}