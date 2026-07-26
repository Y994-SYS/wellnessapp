package com.alkanyazilim.wellnesapp.data.model

import com.alkanyazilim.wellnesapp.ui.workout.ExercisePose

data class ExerciseTemplate(
    val name: String,
    val icon: String,
    val isTimeBased: Boolean,
    val defaultDurationSeconds: Int = 60,
    val defaultSets: Int = 3,
    val defaultReps: Int = 12,
    val pose: ExercisePose,
    val instructions: List<String>
)

val kardiyoTemplates = listOf(
    ExerciseTemplate("Jumping Jack", "🤸", true, defaultDurationSeconds = 45,
        pose = ExercisePose.JUMPING_JACK,
        instructions = listOf(
            "Ayakta düz dur, kollar yanda.",
            "Zıplarken bacakları aç, kolları başının üzerinde birleştir.",
            "Tekrar zıplayıp başlangıç pozisyonuna dön."
        )),
    ExerciseTemplate("Yerinde Koşu", "🏃", true, defaultDurationSeconds = 60,
        pose = ExercisePose.HIGH_KNEE,
        instructions = listOf(
            "Yerinde, dizlerini hafifçe kaldırarak koş.",
            "Kollarını koşar gibi öne arkaya salla.",
            "Tempoyu nefesine göre ayarla."
        )),
    ExerciseTemplate("Burpee", "💥", false, defaultSets = 4, defaultReps = 10,
        pose = ExercisePose.SQUAT,
        instructions = listOf(
            "Ayakta dur, çömel ve ellerini yere koy.",
            "Bacaklarını geriye at, plank pozisyonuna geç.",
            "Bacaklarını tekrar öne çek, ayağa kalkıp zıpla."
        )),
    ExerciseTemplate("Yüksek Diz", "🦵", true, defaultDurationSeconds = 40,
        pose = ExercisePose.HIGH_KNEE,
        instructions = listOf(
            "Yerinde dur, dizlerini karnına doğru yüksekçe kaldır.",
            "Kollarını koşar gibi ters yönde salla.",
            "Hızlı tempoda değiştirerek devam et."
        )),
    ExerciseTemplate("İp Atlama", "🪢", true, defaultDurationSeconds = 90,
        pose = ExercisePose.JUMP_ROPE,
        instructions = listOf(
            "Bilekten ip çevirir gibi elleri hafifçe hareket ettir.",
            "Ayaklarını birlikte hafifçe zıplat.",
            "Dizlerini hafif bük, inişte yumuşak bas."
        )),
    ExerciseTemplate("Mountain Climber", "⛰️", true, defaultDurationSeconds = 45,
        pose = ExercisePose.PLANK,
        instructions = listOf(
            "Plank pozisyonunda başla, eller omuz hizasında.",
            "Dizlerini sırayla göğsüne doğru çek.",
            "Karnını sıkı tutarak tempoyu artır."
        ))
)

val gucTemplates = listOf(
    ExerciseTemplate("Şınav", "💪", false, defaultSets = 3, defaultReps = 12,
        pose = ExercisePose.PUSHUP,
        instructions = listOf(
            "Plank pozisyonunda eller omuz genişliğinde yere bas.",
            "Dirsekleri bükerek göğsünü yere yaklaştır.",
            "Kollarını iterek başlangıç pozisyonuna dön."
        )),
    ExerciseTemplate("Mekik", "🔥", false, defaultSets = 3, defaultReps = 15,
        pose = ExercisePose.SITUP,
        instructions = listOf(
            "Sırt üstü uzan, dizlerini bük, ayaklar yerde.",
            "Ellerini başının arkasında birleştir.",
            "Üst gövdeni dizlerine doğru kaldır, yavaşça in."
        )),
    ExerciseTemplate("Squat", "🏋️", false, defaultSets = 4, defaultReps = 15,
        pose = ExercisePose.SQUAT,
        instructions = listOf(
            "Ayaklarını omuz genişliğinde aç.",
            "Kalçanı geri iterek dizlerini bük.",
            "Uyluklar yere paralel olana kadar in, sonra kalk."
        )),
    ExerciseTemplate("Plank", "🧱", true, defaultDurationSeconds = 45,
        pose = ExercisePose.PLANK,
        instructions = listOf(
            "Dirsekler ve ayak uçları üzerinde dur.",
            "Sırtını düz, karnını sıkı tut.",
            "Pozisyonu süre boyunca koru."
        )),
    ExerciseTemplate("Lunge", "🦿", false, defaultSets = 3, defaultReps = 10,
        pose = ExercisePose.LUNGE,
        instructions = listOf(
            "Bir adım öne at, arka diz yere yaklaşsın.",
            "Ön dizin ayak bileğinin üzerinde kalsın.",
            "İterek başlangıç pozisyonuna dön, bacak değiştir."
        )),
    ExerciseTemplate("Barfiks", "🏗️", false, defaultSets = 3, defaultReps = 8,
        pose = ExercisePose.PULLUP,
        instructions = listOf(
            "Bara omuz genişliğinde asıl.",
            "Çenen bar hizasına gelene kadar kendini çek.",
            "Kontrollü şekilde in."
        ))
)

val esnemeTemplates = listOf(
    ExerciseTemplate("Boyun Esnetme", "🙆", true, defaultDurationSeconds = 30,
        pose = ExercisePose.NECK_STRETCH,
        instructions = listOf(
            "Ayakta veya oturarak dik dur.",
            "Başını yavaşça bir omzuna doğru eğ.",
            "15-20 saniye tut, diğer tarafa geç."
        )),
    ExerciseTemplate("Omuz Esnetme", "🤷", true, defaultDurationSeconds = 30,
        pose = ExercisePose.ARM_STRETCH,
        instructions = listOf(
            "Bir kolunu göğsünün önünden karşı tarafa uzat.",
            "Diğer kolunla dirseğini hafifçe bastır.",
            "Gerginliği hisset, tarafları değiştir."
        )),
    ExerciseTemplate("Kelebek Esnetme", "🦋", true, defaultDurationSeconds = 45,
        pose = ExercisePose.BUTTERFLY,
        instructions = listOf(
            "Otur, ayak tabanlarını birleştir.",
            "Dizlerini yere doğru hafifçe bastır.",
            "Sırtını düz tutarak öne doğru eğil."
        )),
    ExerciseTemplate("Hamstring Esnetme", "🧘", true, defaultDurationSeconds = 45,
        pose = ExercisePose.HAMSTRING_STRETCH,
        instructions = listOf(
            "Otur, bacaklarını öne doğru düz uzat.",
            "Sırtını düz tutarak ellerinle ayaklarına uzan.",
            "Bacak arkasında gerginlik hissedene kadar tut."
        )),
    ExerciseTemplate("Kedi-İnek Pozu", "🐱", true, defaultDurationSeconds = 40,
        pose = ExercisePose.CAT_COW,
        instructions = listOf(
            "Elleri ve dizleri üzerinde dur.",
            "Nefes alırken sırtını çukurlaştır (inek).",
            "Nefes verirken sırtını kamburlaştır (kedi)."
        )),
    ExerciseTemplate("Çocuk Pozu", "🧎", true, defaultDurationSeconds = 60,
        pose = ExercisePose.CHILD_POSE,
        instructions = listOf(
            "Dizlerinin üzerine otur, topuklarına yaslan.",
            "Kollarını öne doğru uzatarak öne eğil.",
            "Alnını yere yaklaştır, rahatça nefes al."
        ))
)

fun templatesForCategory(categoryLabel: String): List<ExerciseTemplate> = when (categoryLabel) {
    "Kardiyo" -> kardiyoTemplates
    "Güç" -> gucTemplates
    "Esneme" -> esnemeTemplates
    else -> emptyList()
}