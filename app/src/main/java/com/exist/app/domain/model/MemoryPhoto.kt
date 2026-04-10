package com.exist.app.domain.model

data class MemoryPhoto(
    val id: Long = 0,
    val imagePath: String,
    val createdAt: Long,
    val dayKey: String,
    val emotionTag: EmotionTag,
    val caption: String,
    val isTemporary: Boolean,
    val expiresAt: Long?,
    val isProofPhoto: Boolean
)
