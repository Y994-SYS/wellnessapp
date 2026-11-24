package com.alkanyazilim.wellnesapp.ui.steps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.repository.HealthConnectManager
import com.alkanyazilim.wellnesapp.ui.util.rememberCurrentLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class CalorieFact(val emoji: String, val name: String, val kcalPerUnit: Double, val unitLabel: String)

private val calorieFacts = listOf(
    CalorieFact("🍟", "patates kızartması", 300.0, "porsiyon"),
    CalorieFact("🍦", "top dondurma", 60.0, "top"),
    CalorieFact("🍕", "dilim pizza", 285.0, "dilim"),
    CalorieFact("🍔", "hamburger", 250.0, "adet"),
    CalorieFact("🥐", "kruvasan", 230.0, "adet"),
    CalorieFact("🍩", "donut", 195.0, "adet"),
    CalorieFact("🍌", "muz", 105.0, "adet"),
    CalorieFact("🍫", "çikolata parçası", 150.0, "adet"),
    // Türk mutfağından karşılaştırmalar
    CalorieFact("🫓", "lahmacun", 250.0, "adet"),
    CalorieFact("🥙", "kıymalı pide", 650.0, "adet"),
    CalorieFact("🍢", "Adana kebap", 600.0, "porsiyon"),
    CalorieFact("🍯", "baklava", 300.0, "dilim"),
    CalorieFact("🥯", "simit", 300.0, "adet"),
    CalorieFact("🧀", "künefe", 400.0, "porsiyon")
)

private sealed class DistanceFact {
    abstract val emoji: String
    data class Climb(override val emoji: String, val name: String, val heightM: Double) : DistanceFact()
    data class Loop(override val emoji: String, val name: String, val loopKm: Double) : DistanceFact()
}

private val distanceFacts = listOf(
    DistanceFact.Climb("🗻", "Japonya'daki Fuji Dağı'nın zirvesine", 3776.0),
    DistanceFact.Climb("🗼", "Paris'teki Eyfel Kulesi'nin tepesine", 330.0),
    DistanceFact.Climb("🏙️", "New York'taki Empire State Building'in tepesine", 443.0),
    DistanceFact.Loop("🏛️", "Mısır piramitlerinin çevresinde", 3.6),
    DistanceFact.Loop("⚽", "bir futbol sahasının çevresinde", 0.338),
    DistanceFact.Loop("🏃", "bir atletizm pistinde (400m)", 0.4),
    // İstanbul'dan karşılaştırmalar
    DistanceFact.Climb("🗼", "İstanbul'daki Çamlıca Kulesi'nin zirvesine", 369.0),
    DistanceFact.Climb("🏯", "İstanbul'daki Galata Kulesi'nin tepesine", 67.0),
    DistanceFact.Loop("🌉", "15 Temmuz Şehitler Köprüsü'nü", 1.56)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsDetailScreen(dateString: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val locale = rememberCurrentLocale()
    val date = LocalDate.parse(dateString)
    val healthConnectManager = remember { HealthConnectManager(context) }

    // Sabit 6500 yerine ayarlardaki gerçek hedef
    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val goalSteps by userPreferences.stepGoal.collectAsState(initial = UserPreferences.DEFAULT_STEP_GOAL)

    var steps by remember { mutableStateOf<Long?>(null) }
    var paceBreakdown by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    LaunchedEffect(dateString) {
        steps = healthConnectManager.readStepsForDate(date)
        paceBreakdown = healthConnectManager.readWalkingPaceBreakdown(date)
    }

    // Gün bazında sabit ama günden güne değişen seçim
    val dayIndex = date.toEpochDay()
    val calorieFact = calorieFacts[(dayIndex.mod(calorieFacts.size.toLong())).toInt()]
    val distanceFact = distanceFacts[(dayIndex.mod(distanceFacts.size.toLong())).toInt()]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (steps == null || paceBreakdown == null) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val stepsValue = steps!!
        val (slowSteps, briskSteps) = paceBreakdown!!
        val distanceKm = stepsValue * 0.000762
        val calories = stepsValue * 0.04
        val progress = (stepsValue.toFloat() / goalSteps.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Text("Yürüme kaydı", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                HalfCircleGauge(progress = progress, steps = stepsValue)
            }

            Spacer(Modifier.height(20.dp))

            PaceBreakdownBar(slowSteps = slowSteps, briskSteps = briskSteps)

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DetailStat("Hedef", "$goalSteps")
                DetailStat("Mesafe", String.format(locale, "%.2f km", distanceKm))
                DetailStat("Yakılan", String.format(locale, "%.0f kcal", calories))
            }

            Spacer(Modifier.height(24.dp))

            val calorieUnits = calories / calorieFact.kcalPerUnit
            FunFactCard(
                emoji = calorieFact.emoji,
                title = "Yakılan toplam kalori",
                value = String.format(locale, "%.0f kcal", calories),
                subtitle = "≈ ${String.format(locale, "%.2f", calorieUnits)} ${calorieFact.unitLabel} ${calorieFact.name}",
                backgroundColor = Color(0xFFF3D9C4)
            )

            Spacer(Modifier.height(16.dp))

            val distanceSubtitle = when (distanceFact) {
                is DistanceFact.Climb -> {
                    val times = (distanceKm * 1000) / distanceFact.heightM
                    "≈ ${distanceFact.name} ${String.format(locale, "%.2f", times)} kez tırmanmaya eşit"
                }
                is DistanceFact.Loop -> {
                    val laps = distanceKm / distanceFact.loopKm
                    "≈ ${distanceFact.name} ${String.format(locale, "%.2f", laps)} tur attınız"
                }
            }

            FunFactCard(
                emoji = distanceFact.emoji,
                title = "Toplam mesafe",
                value = String.format(locale, "%.2f km", distanceKm),
                subtitle = distanceSubtitle,
                backgroundColor = Color(0xFFD6E4EE)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaceBreakdownBar(slowSteps: Long, briskSteps: Long) {
    val total = (slowSteps + briskSteps).coerceAtLeast(1)
    val slowPercent = (slowSteps * 100 / total).toInt()
    val briskPercent = 100 - slowPercent
    val slowFraction = (slowSteps.toFloat() / total.toFloat()).coerceIn(0.02f, 0.98f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$slowSteps · %$slowPercent", fontWeight = FontWeight.Bold)
            Text("$briskSteps · %$briskPercent", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(slowFraction)
                    .fillMaxHeight()
                    .background(Color(0xFFF6B8A9))
            )
            Box(
                modifier = Modifier
                    .weight(1f - slowFraction)
                    .fillMaxHeight()
                    .background(Color(0xFFE05A4E))
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Yavaş yürüyüş", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tempolu yürüyüş", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HalfCircleGauge(progress: Float, steps: Long) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.size(240.dp, 130.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val dashLength = 6.dp.toPx()
            val gapLength = 6.dp.toPx()

            // Yarım daire için çap = genişlik (eski kod yüksekliği 2 ile çarpıp elips çiziyordu)
            val diameter = size.width - strokeWidth
            val arcSize = Size(diameter, diameter)
            val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            val dashed = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength))
            )

            // Arka plan izi: tam yarım daire
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = dashed,
                topLeft = arcTopLeft,
                size = arcSize
            )

            val clampedProgress = progress.coerceIn(0.02f, 1f)
            val totalSweep = 180f * clampedProgress
            val redEnd = (180f * 0.40f).coerceAtMost(totalSweep)
            val yellowEnd = (180f * 0.75f).coerceAtMost(totalSweep)

            if (redEnd > 0f) {
                drawArc(
                    color = Color(0xFFE05A4E),
                    startAngle = 180f,
                    sweepAngle = redEnd,
                    useCenter = false,
                    style = dashed,
                    topLeft = arcTopLeft,
                    size = arcSize
                )
            }
            if (yellowEnd > redEnd) {
                drawArc(
                    color = Color(0xFFF6C244),
                    startAngle = 180f + redEnd,
                    sweepAngle = yellowEnd - redEnd,
                    useCenter = false,
                    style = dashed,
                    topLeft = arcTopLeft,
                    size = arcSize
                )
            }
            if (totalSweep > yellowEnd) {
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = 180f + yellowEnd,
                    sweepAngle = totalSweep - yellowEnd,
                    useCenter = false,
                    style = dashed,
                    topLeft = arcTopLeft,
                    size = arcSize
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Adım", style = MaterialTheme.typography.bodyMedium)
            Text(text = "$steps", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FunFactCard(emoji: String, title: String, value: String, subtitle: String, backgroundColor: Color) {
    // Zemin pastel ve sabit olduğu için yazı rengi de sabit koyu olmalı;
    // aksi halde koyu temada açık renk yazı açık zeminde kaybolur.
    val textColor = Color(0xFF1C1B1F)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = textColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor)
                Spacer(Modifier.height(4.dp))
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = textColor)
            }
            Spacer(Modifier.width(12.dp))
            Text(emoji, fontSize = 48.sp)
        }
    }
}