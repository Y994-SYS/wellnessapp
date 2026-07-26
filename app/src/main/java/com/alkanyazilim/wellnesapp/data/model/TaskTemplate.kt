package com.alkanyazilim.wellnesapp.data.model

import com.alkanyazilim.wellnesapp.data.local.TaskCategory

data class TaskTemplate(
    val title: String,
    val icon: String,
    val category: TaskCategory,
    val isRecurring: Boolean
)

val taskTemplates = listOf(
    TaskTemplate("Su iç", "💧", TaskCategory.SAGLIK, true),
    TaskTemplate("Vitamin al", "💊", TaskCategory.SAGLIK, true),
    TaskTemplate("8 saat uyu", "😴", TaskCategory.SAGLIK, true),
    TaskTemplate("Meditasyon yap", "🧘", TaskCategory.SAGLIK, true),
    TaskTemplate("Diş fırçala", "🦷", TaskCategory.SAGLIK, true),
    TaskTemplate("Yürüyüş yap", "🚶", TaskCategory.SPOR, true),
    TaskTemplate("Koşuya çık", "🏃", TaskCategory.SPOR, true),
    TaskTemplate("Squat yap", "🏋️", TaskCategory.SPOR, true),
    TaskTemplate("Esneme egzersizi", "🤸", TaskCategory.SPOR, true),
    TaskTemplate("Bisiklete bin", "🚴", TaskCategory.SPOR, false),
    TaskTemplate("Kitap oku", "📖", TaskCategory.KISISEL, true),
    TaskTemplate("Günlük tut", "📓", TaskCategory.KISISEL, true),
    TaskTemplate("Aile ile görüş", "👨‍👩‍👧", TaskCategory.KISISEL, false),
    TaskTemplate("Ev temizliği", "🧹", TaskCategory.KISISEL, false),
    TaskTemplate("Alışveriş yap", "🛒", TaskCategory.KISISEL, false)
)