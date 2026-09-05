package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getAll(): Flow<List<BadgeEntity>>

    @Query("SELECT badgeId FROM badges")
    suspend fun getUnlockedIdsOnce(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: BadgeEntity)
}
