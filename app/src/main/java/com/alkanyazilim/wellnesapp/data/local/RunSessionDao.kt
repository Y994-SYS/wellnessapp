package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunSessionDao {

    @Insert
    suspend fun insert(session: RunSessionEntity)

    @Query("SELECT * FROM run_sessions ORDER BY startTimeMillis DESC")
    fun getAll(): Flow<List<RunSessionEntity>>

    @Query("DELETE FROM run_sessions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM run_sessions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    // ---- YENİ: Yedekleme (export/import) için ----

    @Query("DELETE FROM run_sessions")
    suspend fun deleteAll()

    // Import: yedekteki orijinal id'lerle geri yükler
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRestore(sessions: List<RunSessionEntity>)
}