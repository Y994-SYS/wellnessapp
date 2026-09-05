package com.alkanyazilim.wellnesapp.data.repository

import java.time.LocalDate

object BadgeEvaluator {
    fun evaluate(
        hasAnyRun: Boolean,
        maxStepsInSingleRun: Long,
        totalRunDistanceKm: Double,
        maxTaskBestStreak: Int,
        bestWaterStreak: Int
    ): Set<String> {
        val unlocked = mutableSetOf<String>()
        if (hasAnyRun) unlocked += "ilk_adim"
        if (maxStepsInSingleRun >= 10000) unlocked += "10k_kulubu"
        if (bestWaterStreak >= 7) unlocked += "su_ustasi"
        if (maxTaskBestStreak >= 7) unlocked += "aliskanlik_kahramani"
        if (totalRunDistanceKm >= 42.0) unlocked += "maratoncu"
        return unlocked
    }

    // Su tüketim geçmişinden en iyi (tarihsel en uzun) ardışık "hedefe ulaşılan
    // gün" serisini hesaplar. TaskStreakCalculator'daki best-streak mantığıyla
    // aynı yaklaşım: aradaki boşluklarda seri sıfırlanır, en uzun seri döner.
    fun bestWaterStreak(entries: Map<LocalDate, Int>, goalMl: Int): Int {
        val qualifyingDates = entries
            .filter { (_, consumed) -> consumed >= goalMl }
            .keys
            .toSortedSet()
            .toList()

        if (qualifyingDates.isEmpty()) return 0

        var best = 1
        var run = 1
        for (i in 1 until qualifyingDates.size) {
            run = if (qualifyingDates[i] == qualifyingDates[i - 1].plusDays(1)) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }
}
