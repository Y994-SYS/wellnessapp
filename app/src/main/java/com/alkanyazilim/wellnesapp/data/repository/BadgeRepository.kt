package com.alkanyazilim.wellnesapp.data.repository

import com.alkanyazilim.wellnesapp.data.local.BadgeDao
import com.alkanyazilim.wellnesapp.data.local.BadgeEntity
import com.alkanyazilim.wellnesapp.data.local.RunSessionDao
import com.alkanyazilim.wellnesapp.data.local.TaskDao
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class BadgeUiState(
    val definition: BadgeDefinition,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

class BadgeRepository(
    private val badgeDao: BadgeDao,
    private val taskDao: TaskDao,
    private val runSessionDao: RunSessionDao,
    private val waterDataStore: WaterDataStore
) {
    val badgeStates: Flow<List<BadgeUiState>> = badgeDao.getAll().map { unlockedList ->
        val unlockedMap = unlockedList.associateBy { it.badgeId }
        BadgeCatalog.all.map { def ->
            val unlocked = unlockedMap[def.id]
            BadgeUiState(def, unlocked != null, unlocked?.unlockedAt)
        }
    }

    // Tüm ilgili veriyi bir kez okuyup rozet koşullarını değerlendirir,
    // yeni kazanılan rozetleri (varsa) kalıcı olarak kaydeder.
    // Dönüş değeri: bu çağrıda YENİ kazanılan rozet tanımları (kutlama göstermek için).
    suspend fun refreshBadges(): List<BadgeDefinition> {
        val runs = runSessionDao.getAll().first()
        val tasks = taskDao.getAllTasks().first()
        val waterEntries = waterDataStore.allConsumedEntries.first()
        val waterGoal = waterDataStore.dailyGoal.first()

        val hasAnyRun = runs.isNotEmpty()
        val maxSteps = (runs.maxOfOrNull { it.steps } ?: 0).toLong()
        val totalDistanceKm = runs.sumOf { it.steps * 0.000762 }
        val maxTaskBestStreak = tasks.maxOfOrNull { it.bestStreak } ?: 0
        val bestWaterStreak = BadgeEvaluator.bestWaterStreak(waterEntries, waterGoal)

        val shouldBeUnlocked = BadgeEvaluator.evaluate(
            hasAnyRun = hasAnyRun,
            maxStepsInSingleRun = maxSteps,
            totalRunDistanceKm = totalDistanceKm,
            maxTaskBestStreak = maxTaskBestStreak,
            bestWaterStreak = bestWaterStreak
        )

        val alreadyUnlocked = badgeDao.getUnlockedIdsOnce().toSet()
        val newlyUnlockedIds = shouldBeUnlocked - alreadyUnlocked

        val now = System.currentTimeMillis()
        newlyUnlockedIds.forEach { id ->
            badgeDao.insert(BadgeEntity(badgeId = id, unlockedAt = now))
        }

        return BadgeCatalog.all.filter { it.id in newlyUnlockedIds }
    }
}
