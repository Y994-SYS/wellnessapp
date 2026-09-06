package com.alkanyazilim.wellnesapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BadgeEvaluatorTest {

    @Test
    fun `hic kosu yoksa ilk adim kazanilmaz`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = false,
            maxStepsInSingleRun = 0L,
            totalRunDistanceKm = 0.0,
            maxTaskBestStreak = 0,
            bestWaterStreak = 0
        )
        assertFalse(result.contains("ilk_adim"))
    }

    @Test
    fun `bir kosu varsa ilk adim kazanilir`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = true,
            maxStepsInSingleRun = 500L,
            totalRunDistanceKm = 0.5,
            maxTaskBestStreak = 0,
            bestWaterStreak = 0
        )
        assertTrue(result.contains("ilk_adim"))
    }

    @Test
    fun `10000 adim ustunde 10k kulubu kazanilir`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = true,
            maxStepsInSingleRun = 10500L,
            totalRunDistanceKm = 8.0,
            maxTaskBestStreak = 0,
            bestWaterStreak = 0
        )
        assertTrue(result.contains("10k_kulubu"))
    }

    @Test
    fun `9999 adim 10k kulubu kazandirmaz`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = true,
            maxStepsInSingleRun = 9999L,
            totalRunDistanceKm = 7.0,
            maxTaskBestStreak = 0,
            bestWaterStreak = 0
        )
        assertFalse(result.contains("10k_kulubu"))
    }

    @Test
    fun `42 km ustunde maratoncu kazanilir`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = true,
            maxStepsInSingleRun = 5000L,
            totalRunDistanceKm = 42.5,
            maxTaskBestStreak = 0,
            bestWaterStreak = 0
        )
        assertTrue(result.contains("maratoncu"))
    }

    @Test
    fun `7 gunluk gorev streaki aliskanlik kahramani kazandirir`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = false,
            maxStepsInSingleRun = 0L,
            totalRunDistanceKm = 0.0,
            maxTaskBestStreak = 7,
            bestWaterStreak = 0
        )
        assertTrue(result.contains("aliskanlik_kahramani"))
    }

    @Test
    fun `7 gunluk su streaki su ustasi kazandirir`() {
        val result = BadgeEvaluator.evaluate(
            hasAnyRun = false,
            maxStepsInSingleRun = 0L,
            totalRunDistanceKm = 0.0,
            maxTaskBestStreak = 0,
            bestWaterStreak = 7
        )
        assertTrue(result.contains("su_ustasi"))
    }

    @Test
    fun `bos su gecmisinde best streak sifir`() {
        val streak = BadgeEvaluator.bestWaterStreak(emptyMap(), goalMl = 2000)
        assertEquals(0, streak)
    }

    @Test
    fun `ardisik 3 gun hedefi tutturunca best streak 3`() {
        val entries = mapOf(
            LocalDate.of(2026, 9, 1) to 2100,
            LocalDate.of(2026, 9, 2) to 2000,
            LocalDate.of(2026, 9, 3) to 2500
        )
        val streak = BadgeEvaluator.bestWaterStreak(entries, goalMl = 2000)
        assertEquals(3, streak)
    }

    @Test
    fun `hedefin altindaki gunler streak bozar`() {
        val entries = mapOf(
            LocalDate.of(2026, 9, 1) to 2100,
            LocalDate.of(2026, 9, 2) to 1000, // hedefin altında
            LocalDate.of(2026, 9, 3) to 2200,
            LocalDate.of(2026, 9, 4) to 2300
        )
        val streak = BadgeEvaluator.bestWaterStreak(entries, goalMl = 2000)
        assertEquals(2, streak) // sadece 3-4 Eylül ardışık say
    }
}