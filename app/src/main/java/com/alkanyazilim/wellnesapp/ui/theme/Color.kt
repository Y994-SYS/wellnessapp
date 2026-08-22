package com.alkanyazilim.wellnesapp.ui.theme

import androidx.compose.ui.graphics.Color

// Ana marka rengi (mor-indigo) — bottom nav ve kategori vurgularında kullanılan tonla uyumlu
// Purple40: açık temada ana renk (butonlar, aktif sekme)
// Purple80: koyu temada aynı rengin okunabilir/açık versiyonu
val Purple80 = Color(0xFFC9BBFF)
val Purple40 = Color(0xFF7C5CFC)

val PurpleGrey80 = Color(0xFFCFC2DC)
val PurpleGrey40 = Color(0xFF625B71)

// Tertiary: eskiden sert bir "hot pink" idi (0xFFE91E8C), daha yumuşak bir
// mercan-kırmızıya çekildi — Egzersiz ekranı vurgusuyla aynı aile
val Pink80 = Color(0xFFFFB4C0)
val Pink40 = Color(0xFFE85D75)

// Açık tema için özel yüzey/arka plan tonları (kartların belirginleşmesi için)
val LightBackground = Color(0xFF92E7F3)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDE6F7)
val LightPrimaryContainer = Color(0xFFEADDFF)
val LightSecondaryContainer = Color(0xFFE8DEF8)
val LightTertiaryContainer = Color(0xFFFFD8E0)

// Koyu tema için özel yüzey/arka plan tonları
// Arka plan saf siyah yerine mor marka rengiyle uyumlu koyu mor-gri
val DarkBackground = Color(0xFF15141C)
val DarkSurface = Color(0xFF201F2A)
val DarkSurfaceVariant = Color(0xFF2A2836)
val DarkPrimaryContainer = Color(0xFF4A3B90)
val DarkSecondaryContainer = Color(0xFF3E3752)
val DarkTertiaryContainer = Color(0xFF5C2E3E)

// ---- Ekran bazlı vurgu renkleri ----
// Aynı doygunluk/parlaklık ailesinde, birbirinden net ayrışan tonlar.
// AppColors.kt içindeki WaterAccent / StepAccent / TaskAccent / ExerciseAccent /
// HomeAccent tanımlarını bu değerlere göre güncellemen için buraya eklendi.
val WaterAccentColor = Color(0xFF3B9EE8)     // Su — canlı mavi
val StepAccentColor = Color(0xFFFF7A59)      // Adım — mercan/turuncu
val TaskAccentColor = Purple40                // Görevler — ana marka rengiyle aynı
val ExerciseAccentColor = Color(0xFFE85D75)  // Egzersiz — mercan-kırmızı (Pink40 ile aynı)
val HomeAccentColor = Purple40                // Ana Sayfa — nötr, marka rengiyle aynı