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
    val instructions: List<String>,
    val lottieFileName: String,
    val caloriesPerKgPerHour: Double = 5.0
)

val kardiyoTemplates = listOf(
    ExerciseTemplate("Jumping Jack", "🤸", true, defaultDurationSeconds = 45,
        pose = ExercisePose.JUMPING_JACK, lottieFileName = "jumping_jack", caloriesPerKgPerHour = 8.0,
        instructions = listOf(
            "Ayakta düz dur, kollar yanda.",
            "Zıplarken bacakları aç, kolları başının üzerinde birleştir.",
            "Tekrar zıplayıp başlangıç pozisyonuna dön."
        )),
    ExerciseTemplate("Yerinde Koşu", "🏃", true, defaultDurationSeconds = 60,
        pose = ExercisePose.HIGH_KNEE, lottieFileName = "yerinde_kosu", caloriesPerKgPerHour = 8.0,
        instructions = listOf(
            "Yerinde, dizlerini hafifçe kaldırarak koş.",
            "Kollarını koşar gibi öne arkaya salla.",
            "Tempoyu nefesine göre ayarla."
        )),
    ExerciseTemplate("Burpee", "💥", false, defaultSets = 4, defaultReps = 10,
        pose = ExercisePose.SQUAT, lottieFileName = "burpee", caloriesPerKgPerHour = 10.0,
        instructions = listOf(
            "Ayakta dur, çömel ve ellerini yere koy.",
            "Bacaklarını geriye at, plank pozisyonuna geç.",
            "Bacaklarını tekrar öne çek, ayağa kalkıp zıpla."
        )),
    ExerciseTemplate("Yüksek Diz", "🦵", true, defaultDurationSeconds = 40,
        pose = ExercisePose.HIGH_KNEE, lottieFileName = "yuksek_diz", caloriesPerKgPerHour = 8.0,
        instructions = listOf(
            "Yerinde dur, dizlerini karnına doğru yüksekçe kaldır.",
            "Kollarını koşar gibi ters yönde salla.",
            "Hızlı tempoda değiştirerek devam et."
        )),
    ExerciseTemplate("İp Atlama", "🪢", true, defaultDurationSeconds = 90,
        pose = ExercisePose.JUMP_ROPE, lottieFileName = "ip_atlama", caloriesPerKgPerHour = 11.0,
        instructions = listOf(
            "Bilekten ip çevirir gibi elleri hafifçe hareket ettir.",
            "Ayaklarını birlikte hafifçe zıplat.",
            "Dizlerini hafif bük, inişte yumuşak bas."
        )),
    ExerciseTemplate("Mountain Climber", "⛰️", true, defaultDurationSeconds = 45,
        pose = ExercisePose.PLANK, lottieFileName = "mountain_climber", caloriesPerKgPerHour = 8.0,
        instructions = listOf(
            "Plank pozisyonunda başla, eller omuz hizasında.",
            "Dizlerini sırayla göğsüne doğru çek.",
            "Karnını sıkı tutarak tempoyu artır."
        )),
    ExerciseTemplate("Yıldız Zıplama", "🌟", true, defaultDurationSeconds = 40,
        pose = ExercisePose.JUMPING_JACK, lottieFileName = "star_jump", caloriesPerKgPerHour = 11.0,
        instructions = listOf(
            "Havadayken kollarını ve bacaklarını yanlara doğru tam aç.",
            "Hafif bükük dizlerle yumuşak bir şekilde iniş yap.",
            "Harekete yarı çömelme pozisyonunda başla ve bitir."
        )),
    ExerciseTemplate("Kutuya Zıplama", "📦", false, defaultSets = 4, defaultReps = 9,
        pose = ExercisePose.SQUAT, lottieFileName = "box_jump", caloriesPerKgPerHour = 9.0,
        instructions = listOf(
            "Ayağının tamamıyla kutuya yumuşak ve sessizce iniş yap.",
            "Kutunun üzerinde durunca kalçalarını tam açık tut.",
            "Aşil tendonuna yükü azaltmak için aşağı zıplamak yerine adım atarak in."
        )),
    ExerciseTemplate("Kurbağa Sıçraması", "🐸", false, defaultSets = 3, defaultReps = 12,
        pose = ExercisePose.SQUAT, lottieFileName = "frog_jumps", caloriesPerKgPerHour = 9.0,
        instructions = listOf(
            "Eklemleri korumak için tüm tabanla yumuşak iniş yap.",
            "Daha uzağa zıplamak için kollarını kullanarak momentum kazan.",
            "Göğsünü açık tut, yere bakma."
        )),
    ExerciseTemplate("Sıçramalı Squat", "🦘", false, defaultSets = 3, defaultReps = 13,
        pose = ExercisePose.SQUAT, lottieFileName = "jump_squats_bodyweight", caloriesPerKgPerHour = 9.0,
        instructions = listOf(
            "Ayak parmak uçlarınla yumuşak iniş yap ve hemen sıradaki squat'a geç.",
            "Sırtını düz, göğsünü açık tut.",
            "Daha patlayıcı bir itiş için kollarını yukarı savur."
        )),
    ExerciseTemplate("Gölge Boks", "🥊", true, defaultDurationSeconds = 150,
        pose = ExercisePose.STANDING, lottieFileName = "shadow_boxing", caloriesPerKgPerHour = 7.0,
        instructions = listOf(
            "Dizlerini gevşek tut, sürekli ayak parmaklarının ucunda dur.",
            "Yumruklar sadece kollardan değil gövdeden başlamalı.",
            "Sakatlanmayı önlemek için dirsekleri sertçe tam kilitleme."
        )),
    ExerciseTemplate("Hızlı Yürüyüş", "🚶", true, defaultDurationSeconds = 1200,
        pose = ExercisePose.STANDING, lottieFileName = "walking_fast", caloriesPerKgPerHour = 5.5,
        instructions = listOf(
            "Adımlar uzun olmak yerine daha kısa ve hızlı olmalı.",
            "Daha güçlü bir hareket için dirsekleri 90 derece bük.",
            "Daha iyi karın stabilitesi için karın kaslarını bilinçli olarak sık."
        )),
    ExerciseTemplate("Plank Sıçraması", "🤸‍♂️", true, defaultDurationSeconds = 40,
        pose = ExercisePose.PLANK, lottieFileName = "plank_jack", caloriesPerKgPerHour = 7.5,
        instructions = listOf(
            "Üst gövdeni olabildiğince sabit tut, sadece bacaklar hareket etsin.",
            "Ayak parmak uçlarınla yumuşak iniş yap."
        )),
    ExerciseTemplate("Yana Yumruk Atma", "👊", true, defaultDurationSeconds = 50,
        pose = ExercisePose.STANDING, lottieFileName = "side_to_side_punch", caloriesPerKgPerHour = 6.0,
        instructions = listOf(
            "Ayaklarını ve kalçanı yumruk yönüne doğru çevir.",
            "Yumruklar hızlı ve keskin olmalı.",
            "Her yumruktan sonra ellerini hızla yüzünün önündeki savunma pozisyonuna geri getir."
        )),
    ExerciseTemplate("Yerinde Ön Tekme", "🦵", true, defaultDurationSeconds = 45,
        pose = ExercisePose.HIGH_KNEE, lottieFileName = "alternate_front_kick_in_place", caloriesPerKgPerHour = 7.0,
        instructions = listOf(
            "Sırtını düz tut; tekme atarken geriye yaslanma.",
            "Tekmeyi kontrollü at; dizi sertçe 'kilitleme'.",
            "Daha iyi denge için ellerini savunma pozisyonunda tut."
        )),
    ExerciseTemplate("Patlayıcı Şınav", "💥", false, defaultSets = 3, defaultReps = 8,
        pose = ExercisePose.PUSHUP, lottieFileName = "explosive_push_ups", caloriesPerKgPerHour = 6.5,
        instructions = listOf(
            "Yerden itiş elleri yerden kaldıracak kadar patlayıcı olmalı.",
            "İnişte darbeyi hemen emip bir sonraki tekrara geç.",
            "Hareket boyunca vücudunu plank gibi sabit tut."
        )),
    ExerciseTemplate("Plank Adımlama", "🤾", false, defaultSets = 3, defaultReps = 10,
        pose = ExercisePose.LUNGE, lottieFileName = "plank_lunges", caloriesPerKgPerHour = 6.0,
        instructions = listOf(
            "Ayağını tamamen elinin yanına yerleştirmeye çalış.",
            "Arka bacakta gerilmeyi hissetmek için kalçalarını alçak tut."
        )),
    ExerciseTemplate("Yürüyüş", "🚶‍♀️", true, defaultDurationSeconds = 1800,
        pose = ExercisePose.STANDING, lottieFileName = "walking", caloriesPerKgPerHour = 3.5,
        instructions = listOf(
            "Dik bir duruş koru, gözlerin rahatça öne baksın.",
            "Kan dolaşımı için kollarını aktif salla.",
            "Önce topukla bas, sonra ayak parmaklarına doğru yuvarlanarak yumuşak adım at."
        )),
    ExerciseTemplate("Köprü + Çapraz Dağcı", "🌉", true, defaultDurationSeconds = 40,
        pose = ExercisePose.PLANK, lottieFileName = "bridge_mountain_climber_cross", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Kalça köprüsü pozisyonunu koru — kalçaların düşmesine izin verme.",
            "Her dizini çapraz olarak karşı omzuna doğru sür.",
            "Diz çaprazlarken kalçalarını döndürmeden karnını sıkı tut."
        )),
    ExerciseTemplate("Yumruklu Bacak Kıvırma", "🥊", true, defaultDurationSeconds = 50,
        pose = ExercisePose.HIGH_KNEE, lottieFileName = "alternating_hamstring_curl_with_punch", caloriesPerKgPerHour = 7.5,
        instructions = listOf(
            "Daha iyi denge için yumruğu karşı bacakla koordine et.",
            "Yumruklar hızlı ve patlayıcı ama kontrollü olmalı.",
            "Kıvırma fazında olmayan dizleri hafif bükük tut."
        ))
)

val gucTemplates = listOf(
    ExerciseTemplate("Şınav", "💪", false, defaultSets = 3, defaultReps = 12,
        pose = ExercisePose.PUSHUP, lottieFileName = "sinav", caloriesPerKgPerHour = 6.0,
        instructions = listOf(
            "Plank pozisyonunda eller omuz genişliğinde yere bas.",
            "Dirsekleri bükerek göğsünü yere yaklaştır.",
            "Kollarını iterek başlangıç pozisyonuna dön."
        )),
    ExerciseTemplate("Mekik", "🔥", false, defaultSets = 3, defaultReps = 15,
        pose = ExercisePose.SITUP, lottieFileName = "mekik", caloriesPerKgPerHour = 4.5,
        instructions = listOf(
            "Sırt üstü uzan, dizlerini bük, ayaklar yerde.",
            "Ellerini başının arkasında birleştir.",
            "Üst gövdeni dizlerine doğru kaldır, yavaşça in."
        )),
    ExerciseTemplate("Squat", "🏋️", false, defaultSets = 4, defaultReps = 15,
        pose = ExercisePose.SQUAT, lottieFileName = "squat", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Ayaklarını omuz genişliğinde aç.",
            "Kalçanı geri iterek dizlerini bük.",
            "Uyluklar yere paralel olana kadar in, sonra kalk."
        )),
    ExerciseTemplate("Plank", "🧱", true, defaultDurationSeconds = 45,
        pose = ExercisePose.PLANK, lottieFileName = "plank", caloriesPerKgPerHour = 3.0,
        instructions = listOf(
            "Dirsekler ve ayak uçları üzerinde dur.",
            "Sırtını düz, karnını sıkı tut.",
            "Pozisyonu süre boyunca koru."
        )),
    ExerciseTemplate("Lunge", "🦿", false, defaultSets = 3, defaultReps = 10,
        pose = ExercisePose.LUNGE, lottieFileName = "lunge", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Bir adım öne at, arka diz yere yaklaşsın.",
            "Ön dizin ayak bileğinin üzerinde kalsın.",
            "İterek başlangıç pozisyonuna dön, bacak değiştir."
        )),
    ExerciseTemplate("Barfiks", "🏗️", false, defaultSets = 3, defaultReps = 8,
        pose = ExercisePose.PULLUP, lottieFileName = "barfiks", caloriesPerKgPerHour = 6.0,
        instructions = listOf(
            "Bara omuz genişliğinde asıl.",
            "Çenen bar hizasına gelene kadar kendini çek.",
            "Kontrollü şekilde in."
        )),
    ExerciseTemplate("Superman", "🦸", false, defaultSets = 3, defaultReps = 13,
        pose = ExercisePose.PLANK, lottieFileName = "superman", caloriesPerKgPerHour = 3.5,
        instructions = listOf(
            "Boynun nötr kalması için bakışını yere yönlendir.",
            "Kolları ve bacakları aynı anda ve kontrollü kaldır.",
            "Kaldırırken nefesini tutma."
        )),
    ExerciseTemplate("Yan Plank", "🧍", true, defaultDurationSeconds = 45,
        pose = ExercisePose.PLANK, lottieFileName = "side_plank", caloriesPerKgPerHour = 4.0,
        instructions = listOf(
            "Dirseğini tam omzunun altına yerleştir.",
            "Kalçalarını yüksek tut, vücudun düz bir çizgi oluştursun.",
            "Üstteki kolunu kalçanda tut ya da yukarı uzat."
        )),
    ExerciseTemplate("Rus Bükümü", "🌀", false, defaultSets = 3, defaultReps = 30,
        pose = ExercisePose.SITUP, lottieFileName = "russian_twist", caloriesPerKgPerHour = 4.5,
        instructions = listOf(
            "Daha zorlu bir hareket için bacaklarını hafif kaldır.",
            "Dönüş gövdeden başlamalı, sadece kolları savurmamalısın.",
            "Bakışın dönüş yönünü takip etsin."
        )),
    ExerciseTemplate("Duvar Oturuşu", "🧱", true, defaultDurationSeconds = 45,
        pose = ExercisePose.SQUAT, lottieFileName = "wall_sit_bodyweight", caloriesPerKgPerHour = 3.0,
        instructions = listOf(
            "Kalça ve dizler 90 derece açıda olmalı.",
            "Tüm sırtını duvara sıkıca yasla.",
            "Ağırlığını parmak uçlarında değil topuklarında tut."
        )),
    ExerciseTemplate("Bisiklet Mekiği", "🚴", true, defaultDurationSeconds = 40,
        pose = ExercisePose.SITUP, lottieFileName = "bicycles_crunches", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Başını elleriyle çekme; avuçlar sadece hafifçe kulaklara değsin.",
            "Uzatılan bacağı yere olabildiğince yakın tut.",
            "Tempoya odaklan — ne kadar yavaş yaparsan kaslar o kadar yanar."
        )),
    ExerciseTemplate("Baldır Kaldırma", "🦶", false, defaultSets = 3, defaultReps = 17,
        pose = ExercisePose.STANDING, lottieFileName = "bodyweight_calf_raises", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Tam hareket genişliği kullan — parmak uçlarında tam yüksel, topuğu tam indir.",
            "Tepe noktasında dur ve baldır kasını sık.",
            "Daha geniş hareket açıklığı için bir basamak kenarında yap."
        )),
    ExerciseTemplate("Sandalyede Triceps Dips", "🪑", false, defaultSets = 3, defaultReps = 12,
        pose = ExercisePose.PUSHUP, lottieFileName = "chair_triceps_dips", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Sandalyenin sabit olduğundan ve kaymayacağından emin ol.",
            "Hareket boyunca sırtını sandalye kenarına yakın tut.",
            "Dirsekler 90 dereceye gelene kadar in, sonra yukarı it."
        )),
    ExerciseTemplate("Sırtüstü Bacak Kaldırma", "🦵", false, defaultSets = 3, defaultReps = 13,
        pose = ExercisePose.SITUP, lottieFileName = "lying_leg_raise", caloriesPerKgPerHour = 4.0,
        instructions = listOf(
            "Alt sırtını her zaman yere temas halinde tut.",
            "Bacakları yavaş ve kontrollü indir, yere değdirme."
        )),
    ExerciseTemplate("Plankta Omuz Dokunuşu", "✋", false, defaultSets = 3, defaultReps = 20,
        pose = ExercisePose.PLANK, lottieFileName = "plank_shoulder_taps", caloriesPerKgPerHour = 4.5,
        instructions = listOf(
            "Daha iyi denge için ayaklarını hafif daha geniş aç.",
            "Kalçalar sağa sola sallanmamalı, havada sabit dursun."
        )),
    ExerciseTemplate("Köpek Bacak Kaldırma", "🐕", false, defaultSets = 3, defaultReps = 15,
        pose = ExercisePose.CAT_COW, lottieFileName = "fire_hydrant_bodyweight", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Karın kasların sıkı ve sırtın düz şekilde emekleme pozisyonunda başla.",
            "Uyluk yere paralel olana kadar bükülü dizi yana kaldır.",
            "Kalçaların kare kalsın — bacak kalkarken döndürme."
        )),
    ExerciseTemplate("V Kalkışı", "🔺", false, defaultSets = 3, defaultReps = 12,
        pose = ExercisePose.SITUP, lottieFileName = "v_up", caloriesPerKgPerHour = 6.0,
        instructions = listOf(
            "Kollar başının üzerinde uzatılmış şekilde sırtüstü yatarak başla.",
            "Tek akıcı hareketle V şekline katlan.",
            "Tepe noktasında sırtın düz, bacakların gergin olmalı."
        )),
    ExerciseTemplate("Klasik Mekik", "🔥", false, defaultSets = 3, defaultReps = 17,
        pose = ExercisePose.SITUP, lottieFileName = "crunch_floor", caloriesPerKgPerHour = 4.5,
        instructions = listOf(
            "Hareket boyunca alt sırtını yere bastır.",
            "Sadece kürek kemiklerini kaldır — tüm sırtı değil.",
            "Kalkış yüksekliği yerine karın kasılmasına odaklan."
        )),
    ExerciseTemplate("Göğüs Dokunuşlu Şınav", "👐", false, defaultSets = 3, defaultReps = 10,
        pose = ExercisePose.PUSHUP, lottieFileName = "chest_tap_push_up", caloriesPerKgPerHour = 6.0,
        instructions = listOf(
            "Göğsüne dokunmak için el kaldırırken kalçaların yerle hizalı kalsın.",
            "Dokunuşa zaman kalması için şınavı patlayıcı şekilde yukarı it.",
            "Daha iyi denge için ayaklarını hafif daha geniş aç."
        )),
    ExerciseTemplate("Asılı Bacak Kaldırma", "🧗", false, defaultSets = 3, defaultReps = 11,
        pose = ExercisePose.PULLUP, lottieFileName = "hanging_leg_raises", caloriesPerKgPerHour = 5.0,
        instructions = listOf(
            "Karın kaslarının tamamen aktive olması için bacakları kalça hizasının üzerine kaldır.",
            "İniş fazı yavaş ve sallanmadan olmalı.",
            "Düz bacak zor geliyorsa bükülü dizle başla."
        )),
    ExerciseTemplate("Sıçramalı Tek Bacak Squat", "🦵", false, defaultSets = 3, defaultReps = 6,
        pose = ExercisePose.SQUAT, lottieFileName = "jumping_pistol_squat", caloriesPerKgPerHour = 9.0,
        instructions = listOf(
            "Tek bacak üzerinde yavaşça tam pistol squat'a in.",
            "Alt noktadan patlayıcı şekilde yukarı çık ve duran bacağını tam uzat.",
            "Aynı ayak üzerine yumuşak iniş yap ve kontrollü şekilde devam et."
        )),
    ExerciseTemplate("Yan Karın Mekiği", "🌊", false, defaultSets = 3, defaultReps = 15,
        pose = ExercisePose.SITUP, lottieFileName = "oblique_crunch", caloriesPerKgPerHour = 3.5,
        instructions = listOf(
            "Sadece dirseği dize değil, omzu karşı kalçaya götürmeye odaklan.",
            "Kasları sıkarken nefes ver."
        )),
    ExerciseTemplate("Kaldırılmış Bacakla Mekik", "🦿", false, defaultSets = 3, defaultReps = 20,
        pose = ExercisePose.SITUP, lottieFileName = "raised_leg_crunch", caloriesPerKgPerHour = 4.0,
        instructions = listOf(
            "Alt sırtını her zaman yere temas halinde tut.",
            "Bacakları 90 derece açıda tut.",
            "Her kalkışta nefes ver ve karnını sık."
        ))
)

val esnemeTemplates = listOf(
    ExerciseTemplate("Boyun Esnetme", "🙆", true, defaultDurationSeconds = 30,
        pose = ExercisePose.NECK_STRETCH, lottieFileName = "boyun_esnetme", caloriesPerKgPerHour = 2.0,
        instructions = listOf(
            "Ayakta veya oturarak dik dur.",
            "Başını yavaşça bir omzuna doğru eğ.",
            "15-20 saniye tut, diğer tarafa geç."
        )),
    ExerciseTemplate("Omuz Esnetme", "🤷", true, defaultDurationSeconds = 30,
        pose = ExercisePose.ARM_STRETCH, lottieFileName = "omuz_esnetme", caloriesPerKgPerHour = 2.0,
        instructions = listOf(
            "Bir kolunu göğsünün önünden karşı tarafa uzat.",
            "Diğer kolunla dirseğini hafifçe bastır.",
            "Gerginliği hisset, tarafları değiştir."
        )),
    ExerciseTemplate("Kelebek Esnetme", "🦋", true, defaultDurationSeconds = 45,
        pose = ExercisePose.BUTTERFLY, lottieFileName = "kelebek_esnetme", caloriesPerKgPerHour = 2.5,
        instructions = listOf(
            "Otur, ayak tabanlarını birleştir.",
            "Dizlerini yere doğru hafifçe bastır.",
            "Sırtını düz tutarak öne doğru eğil."
        )),
    ExerciseTemplate("Hamstring Esnetme", "🧘", true, defaultDurationSeconds = 45,
        pose = ExercisePose.HAMSTRING_STRETCH, lottieFileName = "hamstring_esnetme", caloriesPerKgPerHour = 2.0,
        instructions = listOf(
            "Otur, bacaklarını öne doğru düz uzat.",
            "Sırtını düz tutarak ellerinle ayaklarına uzan.",
            "Bacak arkasında gerginlik hissedene kadar tut."
        )),
    ExerciseTemplate("Kedi-İnek Pozu", "🐱", true, defaultDurationSeconds = 40,
        pose = ExercisePose.CAT_COW, lottieFileName = "kedi_inek_pozu", caloriesPerKgPerHour = 2.5,
        instructions = listOf(
            "Elleri ve dizleri üzerinde dur.",
            "Nefes alırken sırtını çukurlaştır (inek).",
            "Nefes verirken sırtını kamburlaştır (kedi)."
        )),
    ExerciseTemplate("Çocuk Pozu", "🧎", true, defaultDurationSeconds = 60,
        pose = ExercisePose.CHILD_POSE, lottieFileName = "cocuk_pozu", caloriesPerKgPerHour = 2.0,
        instructions = listOf(
            "Dizlerinin üzerine otur, topuklarına yaslan.",
            "Kollarını öne doğru uzatarak öne eğil.",
            "Alnını yere yaklaştır, rahatça nefes al."
        )),
    ExerciseTemplate("Asılı Gerinme", "🧗‍♂️", true, defaultDurationSeconds = 45,
        pose = ExercisePose.PULLUP, lottieFileName = "dead_hang_stretch", caloriesPerKgPerHour = 3.0,
        instructions = listOf(
            "Omurgada gerilmeyi hissetmek için kalça ve bacaklarını tamamen gevşet.",
            "Daha 'aktif' bir asılış istiyorsan kürek kemiklerini nazikçe aşağı çek.",
            "Tüm asılma süresi boyunca derin ve düzenli nefes al."
        )),
    ExerciseTemplate("Hafif Triceps Esnetme", "💪", true, defaultDurationSeconds = 30,
        pose = ExercisePose.ARM_STRETCH, lottieFileName = "triceps_light_stretch", caloriesPerKgPerHour = 2.0,
        instructions = listOf(
            "Kolunu başının arkasına bük, diğer elinle dirseğini nazikçe aşağı it.",
            "Esnetme boyunca derin ve serbestçe nefes al.",
            "Omurgan nötr kalsın; alt sırtını kamburlaştırma."
        )),
    ExerciseTemplate("Üst Sırt Esnetme", "🙆‍♂️", true, defaultDurationSeconds = 38,
        pose = ExercisePose.ARM_STRETCH, lottieFileName = "upper_back_stretch", caloriesPerKgPerHour = 2.0,
        instructions = listOf(
            "Parmaklarını önünde kenetle ve avuçlarını göğsünden uzağa it.",
            "Daha fazla esneme için çeneni nazikçe göğsüne indir.",
            "Kürek kemiklerini olabildiğince ayırmaya çalıştığını hayal et."
        ))
)

fun templatesForCategory(categoryLabel: String): List<ExerciseTemplate> = when (categoryLabel) {
    "Kardiyo" -> kardiyoTemplates
    "Güç" -> gucTemplates
    "Esneme" -> esnemeTemplates
    else -> emptyList()
}