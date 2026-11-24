package com.alkanyazilim.wellnesapp.data.repository

import com.alkanyazilim.wellnesapp.data.local.BadgeDao
import com.alkanyazilim.wellnesapp.data.local.BadgeEntity
import com.alkanyazilim.wellnesapp.data.local.RunSessionDao
import com.alkanyazilim.wellnesapp.data.local.TaskDao
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class BadgeUiState(
    val definition: BadgeDefinition,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

class BadgeRepository(
    private val badgeDao: BadgeDao,
    private val taskDao: TaskDao,
    private val runSessionDao: RunSessionDao,
    private val waterDataStore: WaterDataStore,
    private val healthConnectManager: HealthConnectManager
) {
    val badgeStates: Flow<List<BadgeUiState>> = badgeDao.getAll().map { unlockedList ->
        val unlockedMap = unlockedList.associateBy { it.badgeId }
        BadgeCatalog.all.map { def ->
            val unlocked = unlockedMap[def.id]
            BadgeUiState(def, unlocked != null, unlocked?.unlockedAt)
        }
    }

    // Günlük adım rozeti için geriye dönük kaç gün taranacak.
    // Health Connect'te izin verildikten sonra varsayılan olarak ~30 gün geriye okunabilir.
    companion object {
        private const val STEP_LOOKBACK_DAYS = 30L

        // Her yerden (MainActivity, Adım ekranı, Ayarlar) aynı şekilde repo kurmak için.
        fun create(context: android.content.Context): BadgeRepository {
            val appContext = context.applicationContext
            val db = com.alkanyazilim.wellnesapp.data.local.AppDatabase.getInstance(appContext)
            return BadgeRepository(
                db.badgeDao(),
                db.taskDao(),
                db.runSessionDao(),
                WaterDataStore(appContext),
                HealthConnectManager(appContext)
            )
        }
    }

    // Health Connect izni yoksa / servis yoksa rozet hesabı çökmesin: 0 döner.
    // Rozet kalıcı olduğu için zaten açılmışsa tekrar taramaya gerek yok.
    private suspend fun maxDailySteps(alreadyUnlocked: Set<String>): Long {
        if ("10k_kulubu" in alreadyUnlocked) return 0L
        val today = LocalDate.now()
        var best = 0L
        for (offset in 0..STEP_LOOKBACK_DAYS) {
            val steps = runCatching { healthConnectManager.readStepsForDate(today.minusDays(offset)) }
                .getOrNull() ?: continue
            if (steps > best) best = steps
            if (best >= 10_000L) break // eşik geçildi, daha fazla taramaya gerek yok
        }
        return best
    }

    // Tüm ilgili veriyi bir kez okuyup rozet koşullarını değerlendirir,
    // yeni kazanılan rozetleri (varsa) kalıcı olarak kaydeder.
    // Dönüş değeri: bu çağrıda YENİ kazanılan rozet tanımları (kutlama göstermek için).
    suspend fun refreshBadges(): List<BadgeDefinition> {
        val runs = runSessionDao.getAll().first()
        val tasks = taskDao.getAllTasks().first()
        val waterEntries = waterDataStore.allConsumedEntries.first()
        val waterGoal = waterDataStore.dailyGoal.first()
        val alreadyUnlocked = badgeDao.getUnlockedIdsOnce().toSet()

        val hasAnyRun = runs.isNotEmpty()
        val totalDistanceKm = runs.sumOf { it.steps * 0.000762 }
        val maxTaskBestStreak = tasks.maxOfOrNull { it.bestStreak } ?: 0
        val bestWaterStreak = BadgeEvaluator.bestWaterStreak(waterEntries, waterGoal)
        val maxDailySteps = maxDailySteps(alreadyUnlocked)

        val shouldBeUnlocked = BadgeEvaluator.evaluate(
            hasAnyRun = hasAnyRun,
            maxDailySteps = maxDailySteps,
            totalRunDistanceKm = totalDistanceKm,
            maxTaskBestStreak = maxTaskBestStreak,
            bestWaterStreak = bestWaterStreak
        )

        val newlyUnlockedIds = shouldBeUnlocked - alreadyUnlocked

        val now = System.currentTimeMillis()
        newlyUnlockedIds.forEach { id ->
            badgeDao.insert(BadgeEntity(badgeId = id, unlockedAt = now))
        }

        return BadgeCatalog.all.filter { it.id in newlyUnlockedIds }
    }
}