package com.exist.app.domain.repository

import com.exist.app.domain.model.AppSettings
import com.exist.app.domain.model.DaySummary
import com.exist.app.domain.model.EmotionTag
import com.exist.app.domain.model.MemoryPhoto
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    val settings: Flow<AppSettings>

    fun observeTodayProofStatus(): Flow<Boolean>
    fun observeTodayPhotos(): Flow<List<MemoryPhoto>>
    fun observePhotosForDay(dayKey: String): Flow<List<MemoryPhoto>>
    fun observeTimeline(): Flow<List<DaySummary>>
    fun observeMemory(memoryId: Long): Flow<MemoryPhoto?>

    suspend fun savePhoto(
        imagePath: String,
        emotionTag: EmotionTag,
        caption: String,
        isTemporary: Boolean
    )

    suspend fun updateMemory(
        memoryId: Long,
        emotionTag: EmotionTag,
        caption: String,
        isTemporary: Boolean
    ): Boolean

    suspend fun deleteMemory(memoryId: Long): Boolean

    suspend fun deleteExpiredPhotos(): Int
    suspend fun setRandomPromptEnabled(enabled: Boolean)
    suspend fun setRandomVideoChallengeEnabled(enabled: Boolean)
    suspend fun setTemporaryDurationDays(days: Int)
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun setDisplayName(name: String)
    suspend fun setProfileTagline(tagline: String)
    suspend fun setProfilePhotoUri(uri: String)
}
