package com.alkanyazilim.wellnesapp.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Ortak nötr taban — her sayfada aynı
    val Background = Color(0xFFFAF9FC)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1C1B1F)
    val TextSecondary = Color(0xFF6E6E76)

    // Sayfa vurgu renkleri — renk çemberinde uyumlu aralıklı
    val HomeAccent = Color(0xFF7C5CBF)      // Mor
    val StepsAccent = Color(0xFFFF7043)     // Turuncu
    val WaterAccent = Color(0xFF0B84C4)     // Mavi
    val TasksAccent = Color(0xFF43A047)     // Yeşil
    val ExerciseAccent = Color(0xFFD6477B)  // Mercan/Pembe

    // Egzersiz alt kategori renkleri (mevcut, ExerciseAccent ailesinden türetilmiş hissi versin diye aynen korunuyor)
    val ExerciseCardio = Color(0xFF7E57C2)
    val ExerciseStrength = Color(0xFF1E88E5)
    val ExerciseStretch = Color(0xFF43A047)
}