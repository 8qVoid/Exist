package com.exist.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_photos",
    indices = [Index("dayKey"), Index("createdAt")]
)
data class MemoryPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val createdAt: Long,
    val dayKey: String,
    val emotionTag: String,
    val caption: String,
    val isTemporary: Boolean,
    val expiresAt: Long?,
    val isProofPhoto: Boolean
)
