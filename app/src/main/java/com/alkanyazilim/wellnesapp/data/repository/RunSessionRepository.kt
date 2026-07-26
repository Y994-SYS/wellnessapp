package com.alkanyazilim.wellnesapp.data.repository

import com.alkanyazilim.wellnesapp.data.local.RunSessionDao
import com.alkanyazilim.wellnesapp.data.local.RunSessionEntity
import kotlinx.coroutines.flow.Flow

class RunSessionRepository(private val dao: RunSessionDao) {

    val allSessions: Flow<List<RunSessionEntity>> = dao.getAll()

    suspend fun saveSession(session: RunSessionEntity) = dao.insert(session)

    suspend fun deleteSession(id: Int) = dao.deleteById(id)

    suspend fun deleteSessions(ids: List<Int>) = dao.deleteByIds(ids)
}