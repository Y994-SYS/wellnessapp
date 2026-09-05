package com.alkanyazilim.wellnesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val badgeId: String,
    val unlockedAt: Long
)
