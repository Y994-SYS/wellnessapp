package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkanyazilim.wellnesapp.data.model.ExerciseTemplate
import com.alkanyazilim.wellnesapp.data.model.VoiceCue
import com.alkanyazilim.wellnesapp.data.model.templatesForCategory
import com.alkanyazilim.wellnesapp.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.util.Locale
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

private enum class ExerciseMode { SURELI, SET_BAZLI }
private enum class SessionState { FORM, ACTIVE, RESTING, FINISHED }

@Composable
fun CustomExerciseScreen(
    categoryLabel: String,
    accentColor: Color = AppColors.ExerciseCardio,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var exerciseName by rememberSaveable(categoryLabel) { mutableStateOf("") }
    var selectedIcon by rememberSaveable(categoryLabel) { mutableStateOf("🏋️") }
    var mode by rememberSaveable(categoryLabel) { mutableStateOf(ExerciseMode.SURELI) }
    var durationSeconds by rememberSaveable(categoryLabel) { mutableStateOf(60) }
    var totalSets by rememberSaveable(categoryLabel) { mutableStateOf(3) }
    var repsPerSet by rememberSaveable(categoryLabel) { mutableStateOf(12) }

    var sessionState by rememberSaveable(categoryLabel) { mutableStateOf(SessionState.FORM) }
    var currentSet by rememberSaveable(categoryLabel) { mutableStateOf(1) }
    var remainingSeconds by rememberSaveable(categoryLabel) { mutableStateOf(0) }
    var selectedPoseOrdinal by rememberSaveable(categoryLabel) { mutableStateOf(-1) }
    var selectedInstructions by rememberSaveable(categoryLabel) { mutableStateOf(listOf<String>()) }

    // YENİ: Seçili şablonun sesli koçluk verisi. VoiceCue Parcelable olmadığı için
    // rememberSaveable yerine remember kullanılıyor (tts değişkeniyle aynı yaklaşım) —
    // konfigürasyon değişiminde kaybolur ama aktif oturum sırasında sorun yaratmaz.
    var selectedCycleSeconds by remember(categoryLabel) { mutableStateOf(3.0) }
    var selectedVoiceCues by remember(categoryLabel) { mutableStateOf(listOf<VoiceCue>()) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("tr", "TR")
            }
        }
        tts = instance
        onDispose {
            instance.stop()
            instance.shutdown()
        }
    }

    // Önemli sistem anonsları (başlama, bitiş, set geçişi): önceki sözü keser, hemen konuşur.
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // YENİ: Sesli koçluk komutları (hareket talimatları): sıraya eklenir, birbirini
    // kesmez. Hızlı tempolu hareketlerde art arda net bir şekilde duyulmasını sağlar.
    fun speakQueued(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    LaunchedEffect(sessionState, remainingSeconds) {
        if (sessionState == SessionState.ACTIVE && mode == ExerciseMode.SURELI) {
            if (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            } else {
                speak("Süre bitti, harika iş çıkardın")
                sessionState = SessionState.FINISHED
            }
        }
    }

    LaunchedEffect(sessionState, remainingSeconds) {
        if (sessionState == SessionState.RESTING) {
            if (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            } else {
                if (currentSet < totalSets) {
                    currentSet++
                    speak("Set $currentSet, başla")
                    sessionState = SessionState.ACTIVE
                } else {
                    speak("Tebrikler, tüm setleri tamamladın")
                    sessionState = SessionState.FINISHED
                }
            }
        }
    }

    // YENİ: Sesli koçluk motoru.
    // Şablonda voiceCues tanımlıysa, egzersiz aktifken hareketin doğru zamanlamasıyla
    // adım adım komut verir (örn. "Aşağı in... Tut... Kalk").
    //   - Süreli modda: toplam süre boyunca döngü kendini tekrarlar (jumping jack gibi
    //     hızlı hareketlerde kısa döngü, plank gibi duruşlarda uzun bir hatırlatma döngüsü).
    //   - Set bazlı modda: döngü, set başına tekrar sayısı kadar çalışır ve her tekrar
    //     başında "3. tekrar: Aşağı in" gibi sayıyı da söyler.
    // Bu efekt (sessionState, currentSet, mode) değiştiğinde otomatik olarak iptal edilip
    // yeniden başlar — bu yüzden Durdur/Bitir gibi durumlarda ekstra bir "durdurma" koduna
    // gerek yok, yapısal eşzamanlılık (structured concurrency) bunu kendiliğinden halleder.
    // Not: TTS'in gerçek konuşma süresi ile hedeflenen zamanlama arasında küçük sapmalar
    // olabilir — bu bir metronom değil, yaklaşık bir sesli koçtur.
    LaunchedEffect(sessionState, currentSet, mode) {
        if (sessionState != SessionState.ACTIVE) return@LaunchedEffect
        if (selectedVoiceCues.isEmpty()) return@LaunchedEffect

        val cycleMillis = (selectedCycleSeconds * 1000).toLong().coerceAtLeast(300L)
        val sortedCues = selectedVoiceCues.sortedBy { it.atPercent }

        val cycleLimitMillis = when (mode) {
            ExerciseMode.SURELI -> durationSeconds.toLong() * 1000L
            ExerciseMode.SET_BAZLI -> repsPerSet.toLong() * cycleMillis
        }
        if (cycleLimitMillis <= 0L) return@LaunchedEffect

        val startElapsed = System.currentTimeMillis()
        var cycleIndex = 0

        while (true) {
            val cycleStart = cycleIndex * cycleMillis
            if (cycleStart >= cycleLimitMillis) break

            for (cue in sortedCues) {
                val targetElapsed = cycleStart + (cue.atPercent * cycleMillis).toLong()
                val now = System.currentTimeMillis() - startElapsed
                val waitMs = targetElapsed - now
                if (waitMs > 0) delay(waitMs)

                val text = if (mode == ExerciseMode.SET_BAZLI && cue.atPercent <= 0f) {
                    "${cycleIndex + 1}. tekrar: ${cue.text}"
                } else {
                    cue.text
                }
                speakQueued(text)
            }
            cycleIndex++
        }
    }

    when (sessionState) {
        SessionState.FORM -> ExerciseForm(
            categoryLabel = categoryLabel,
            accentColor = accentColor,
            exerciseName = exerciseName,
            onNameChange = { exerciseName = it },
            selectedIcon = selectedIcon,
            onIconChange = { selectedIcon = it },
            mode = mode,
            onModeChange = { mode = it },
            durationSeconds = durationSeconds,
            onDurationChange = { durationSeconds = it },
            totalSets = totalSets,
            onTotalSetsChange = { totalSets = it },
            repsPerSet = repsPerSet,
            onRepsChange = { repsPerSet = it },
            onTemplateSelected = { template ->
                exerciseName = template.name
                selectedIcon = template.icon
                mode = if (template.isTimeBased) ExerciseMode.SURELI else ExerciseMode.SET_BAZLI
                durationSeconds = template.defaultDurationSeconds
                totalSets = template.defaultSets
                repsPerSet = template.defaultReps
                selectedPoseOrdinal = template.pose.ordinal
                selectedInstructions = template.instructions
                selectedCycleSeconds = template.cycleSeconds
                selectedVoiceCues = template.voiceCues
            },
            onStart = {
                currentSet = 1
                if (mode == ExerciseMode.SURELI) {
                    remainingSeconds = durationSeconds
                    speak("$exerciseName başlıyor")
                } else {
                    speak("Set 1, başla")
                }
                sessionState = SessionState.ACTIVE
            },
            selectedPoseOrdinal = selectedPoseOrdinal,
            selectedInstructions = selectedInstructions
        )

        SessionState.ACTIVE -> ActiveExerciseView(
            exerciseName = exerciseName,
            icon = selectedIcon,
            accentColor = accentColor,
            mode = mode,
            remainingSeconds = remainingSeconds,
            currentSet = currentSet,
            totalSets = totalSets,
            repsPerSet = repsPerSet,
            hasVoiceCoaching = selectedVoiceCues.isNotEmpty(),
            onSetComplete = {
                if (currentSet < totalSets) {
                    speak("Set $currentSet tamamlandı, dinlenme zamanı")
                    remainingSeconds = 30
                    sessionState = SessionState.RESTING
                } else {
                    speak("Tebrikler, tüm setleri tamamladın")
                    sessionState = SessionState.FINISHED
                }
            },
            onStop = { sessionState = SessionState.FORM }
        )

        SessionState.RESTING -> RestingView(
            remainingSeconds = remainingSeconds,
            nextSet = currentSet + 1,
            accentColor = accentColor,
            onSkipRest = { remainingSeconds = 0 }
        )

        SessionState.FINISHED -> FinishedView(
            exerciseName = exerciseName,
            accentColor = accentColor,
            onDone = { sessionState = SessionState.FORM }
        )
    }
}

@Composable
private fun ExerciseForm(
    categoryLabel: String,
    accentColor: Color,
    exerciseName: String,
    onNameChange: (String) -> Unit,
    selectedIcon: String,
    onIconChange: (String) -> Unit,
    mode: ExerciseMode,
    onModeChange: (ExerciseMode) -> Unit,
    durationSeconds: Int,
    onDurationChange: (Int) -> Unit,
    totalSets: Int,
    onTotalSetsChange: (Int) -> Unit,
    repsPerSet: Int,
    onRepsChange: (Int) -> Unit,
    onTemplateSelected: (ExerciseTemplate) -> Unit,
    onStart: () -> Unit,
    selectedPoseOrdinal: Int,
    selectedInstructions: List<String>
) {
    val templates = templatesForCategory(categoryLabel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "$categoryLabel Egzersizi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(20.dp))

        if (templates.isNotEmpty()) {
            Text(
                "Hazır Egzersizler",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(templates) { template ->
                    ExerciseTemplateChip(
                        template = template,
                        isSelected = exerciseName == template.name,
                        accentColor = accentColor,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))
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
                value = exerciseName,
                onValueChange = onNameChange,
                label = { Text("Egzersiz adı") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                )
            )
        }

        if (selectedPoseOrdinal >= 0) {
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.12f), accentColor.copy(alpha = 0.04f))
                                )
                            )
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val pose = ExercisePose.values()[selectedPoseOrdinal]
                        val drawableId = drawableForPose(pose)

                        if (drawableId != null) {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(160.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            StickFigure(pose = pose, color = accentColor)
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Nasıl yapılır?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = AppColors.TextPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        selectedInstructions.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Mod",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == ExerciseMode.SURELI,
                onClick = { onModeChange(ExerciseMode.SURELI) },
                label = { Text("Süreli") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Surface,
                    labelColor = AppColors.TextPrimary
                )
            )
            FilterChip(
                selected = mode == ExerciseMode.SET_BAZLI,
                onClick = { onModeChange(ExerciseMode.SET_BAZLI) },
                label = { Text("Set bazlı") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Surface,
                    labelColor = AppColors.TextPrimary
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        if (mode == ExerciseMode.SURELI) {
            Text(
                "Süre",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { onDurationChange((durationSeconds - 15).coerceAtLeast(15)) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) {
                    Text("- 15sn")
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%d:%02d", durationSeconds / 60, durationSeconds % 60),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { onDurationChange(durationSeconds + 15) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) {
                    Text("+ 15sn")
                }
            }
        } else {
            Text(
                "Set Sayısı",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { onTotalSetsChange((totalSets - 1).coerceAtLeast(1)) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) { Text("-") }
                Spacer(Modifier.width(16.dp))
                Text(
                    "$totalSets set",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { onTotalSetsChange(totalSets + 1) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) { Text("+") }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Tekrar Sayısı (set başına)",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { onRepsChange((repsPerSet - 1).coerceAtLeast(1)) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) { Text("-") }
                Spacer(Modifier.width(16.dp))
                Text(
                    "$repsPerSet tekrar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { onRepsChange(repsPerSet + 1) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) { Text("+") }
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onStart,
            enabled = exerciseName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Başla", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
private fun ExerciseTemplateChip(
    template: ExerciseTemplate,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(88.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(if (isSelected) 6.dp else 2.dp, shape = RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isSelected)
                            listOf(accentColor, accentColor.copy(alpha = 0.7f))
                        else
                            listOf(accentColor.copy(alpha = 0.15f), accentColor.copy(alpha = 0.08f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(template.icon, fontSize = 30.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = template.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            textAlign = TextAlign.Center,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary
        )
    }
}

@Composable
private fun ActiveExerciseView(
    exerciseName: String,
    icon: String,
    accentColor: Color,
    mode: ExerciseMode,
    remainingSeconds: Int,
    currentSet: Int,
    totalSets: Int,
    repsPerSet: Int,
    hasVoiceCoaching: Boolean,
    onSetComplete: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(icon, fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                exerciseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            if (hasVoiceCoaching) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔊", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Sesli koçluk açık",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (mode == ExerciseMode.SURELI) {
                Text(
                    text = String.format(Locale.getDefault(), "%d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            } else {
                Text(
                    "Set $currentSet / $totalSets",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$repsPerSet tekrar hedefi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary
                )
            }

            Spacer(Modifier.height(40.dp))

            if (mode == ExerciseMode.SET_BAZLI) {
                Button(
                    onClick = onSetComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Seti Tamamla", fontSize = 18.sp, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.TextSecondary
                )
            ) {
                Text("Durdur")
            }
        }
    }
}

@Composable
private fun RestingView(remainingSeconds: Int, nextSet: Int, accentColor: Color, onSkipRest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Dinlenme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "$remainingSeconds sn",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Sıradaki: Set $nextSet",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = onSkipRest,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = accentColor
                )
            ) {
                Text("Dinlenmeyi Atla")
            }
        }
    }
}

@Composable
private fun FinishedView(exerciseName: String, accentColor: Color, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("🎉", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "$exerciseName tamamlandı!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Tamam", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}