package com.alkanyazilim.wellnesapp.data.repository

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
)

object BadgeCatalog {
    val all = listOf(
        BadgeDefinition("ilk_adim", "İlk Adım", "İlk koşunu tamamla", "🏃"),
        BadgeDefinition("10k_kulubu", "10K Kulübü", "Tek günde 10.000 adım at", "👟"),
        BadgeDefinition("su_ustasi", "Su Ustası", "7 gün üst üste su hedefini tamamla", "💧"),
        BadgeDefinition("aliskanlik_kahramani", "Alışkanlık Kahramanı", "Bir görevde 7 günlük seri yakala", "🔥"),
        BadgeDefinition("maratoncu", "Maratoncu", "Toplam 42 km koş", "🏅")
    )
}
