package com.alkanyazilim.wellnesapp.data.repository

import com.alkanyazilim.wellnesapp.data.local.TaskCompletionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TaskStreakCalculatorTest {

    private fun completion(taskId: Int, date: String, done: Boolean = true) =
        TaskCompletionEntity(taskId = taskId, date = date, isCompleted = done)

    @Test
    fun `bos gecmis sifir streak doner`() {
        val (current, best) = TaskStreakCalculator.calculate(emptyList())
        assertEquals(0, current)
        assertEquals(0, best)
    }

    @Test
    fun `ardisik 3 gun current ve best 3 olur`() {
        val today = LocalDate.of(2026, 9, 5)
        val completions = listOf(
            completion(1, "2026-09-03"),
            completion(1, "2026-09-04"),
            completion(1, "2026-09-05")
        )
        val (current, best) = TaskStreakCalculator.calculate(completions, today)
        assertEquals(3, current)
        assertEquals(3, best)
    }

    @Test
    fun `bugun isaretlenmemisse dunden geriye sayar`() {
        val today = LocalDate.of(2026, 9, 5)
        val completions = listOf(
            completion(1, "2026-09-03"),
            completion(1, "2026-09-04")
        )
        val (current, _) = TaskStreakCalculator.calculate(completions, today)
        assertEquals(2, current)
    }

    @Test
    fun `arada bosluk varsa current sifirlanir ama best korunur`() {
        val today = LocalDate.of(2026, 9, 5)
        val completions = listOf(
            completion(1, "2026-08-20"),
            completion(1, "2026-08-21"),
            completion(1, "2026-08-22"),
            completion(1, "2026-08-23"),
            completion(1, "2026-08-24") // 5 günlük geçmiş bir streak, sonra boşluk
        )
        val (current, best) = TaskStreakCalculator.calculate(completions, today)
        assertEquals(0, current)
        assertEquals(5, best)
    }

    @Test
    fun `isCompleted false olan kayitlar sayilmaz`() {
        val today = LocalDate.of(2026, 9, 5)
        val completions = listOf(
            completion(1, "2026-09-04"),
            completion(1, "2026-09-05", done = false)
        )
        val (current, _) = TaskStreakCalculator.calculate(completions, today)
        assertEquals(1, current) // bugün false, dünden say
    }
}