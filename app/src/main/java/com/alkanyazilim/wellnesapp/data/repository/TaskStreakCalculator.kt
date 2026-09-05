package com.alkanyazilim.wellnesapp.data.repository

import com.alkanyazilim.wellnesapp.data.local.TaskCompletionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Bir görevin tüm tamamlama geçmişinden (task_completions) o anki (current)
 * ve en iyi (best) streak'i saf/deterministik şekilde hesaplar.
 * Herhangi bir Room/DAO bağımlılığı yok — bu yüzden kolayca birim test edilebilir.
 */
object TaskStreakCalculator {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun calculate(completions: List<TaskCompletionEntity>, today: LocalDate = LocalDate.now()): Pair<Int, Int> {
        val completedDates = completions
            .filter { it.isCompleted }
            .map { LocalDate.parse(it.date, formatter) }
            .toSortedSet()

        if (completedDates.isEmpty()) return 0 to 0

        // Best streak: tüm tarihler üzerinde en uzun ardışık günler dizisini bul
        var best = 1
        var run = 1
        val sortedList = completedDates.toList()
        for (i in 1 until sortedList.size) {
            run = if (sortedList[i] == sortedList[i - 1].plusDays(1)) run + 1 else 1
            if (run > best) best = run
        }

        // Current streak: bugünden (ya da dünden, bugün henüz işaretlenmemişse)
        // geriye doğru kaç gün kesintisiz tamamlanmış
        var current = 0
        var cursor = if (completedDates.contains(today)) today else today.minusDays(1)
        while (completedDates.contains(cursor)) {
            current++
            cursor = cursor.minusDays(1)
        }

        return current to best
    }
}
