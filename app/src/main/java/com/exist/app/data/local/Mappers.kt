package com.exist.app.data.local

import com.exist.app.domain.model.EmotionTag
import com.exist.app.domain.model.MemoryPhoto

fun MemoryPhotoEntity.toDomain(): MemoryPhoto {
    return MemoryPhoto(
        id = id,
        imagePath = imagePath,
        createdAt = createdAt,
        dayKey = dayKey,
        emotionTag = EmotionTag.fromName(emotionTag),
        caption = caption,
        isTemporary = isTemporary,
        expiresAt = expiresAt,
        isProofPhoto = isProofPhoto
    )
}

fun MemoryPhoto.toEntity(): MemoryPhotoEntity {
    return MemoryPhotoEntity(
        id = id,
        imagePath = imagePath,
        createdAt = createdAt,
        dayKey = dayKey,
        emotionTag = emotionTag.name,
        caption = caption,
        isTemporary = isTemporary,
        expiresAt = expiresAt,
        isProofPhoto = isProofPhoto
    )
}
