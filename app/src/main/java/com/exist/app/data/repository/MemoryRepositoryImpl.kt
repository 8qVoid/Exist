package com.exist.app.data.repository

import com.exist.app.core.util.DateUtils
import com.exist.app.data.local.MemoryPhotoDao
import com.exist.app.data.local.toDomain
import com.exist.app.data.local.toEntity
import com.exist.app.data.preferences.SettingsPreferences
import com.exist.app.domain.model.AppSettings
import com.exist.app.domain.model.DaySummary
import com.exist.app.domain.model.EmotionTag
import com.exist.app.domain.model.MemoryPhoto
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.LocalDate

class MemoryRepositoryImpl(
    private val dao: MemoryPhotoDao,
    private val settingsPreferences: SettingsPreferences
) : MemoryRepository {

    override val settings: Flow<AppSettings> = settingsPreferences.settings

    override fun observeTodayProofStatus(): Flow<Boolean> {
        val now = DateUtils.nowMillis()
        val dayKey = DateUtils.todayDayKey()
        return dao.observeHasProofForDay(dayKey, now)
    }

    override fun observeTodayPhotos(): Flow<List<MemoryPhoto>> {
        val now = DateUtils.nowMillis()
        val dayKey = DateUtils.todayDayKey()
        return dao.observePhotosForDay(dayKey, now).map { list -> list.map { it.toDomain() } }
    }

    override fun observePhotosForDay(dayKey: String): Flow<List<MemoryPhoto>> {
        return dao.observePhotosForDay(dayKey, DateUtils.nowMillis())
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeTimeline(): Flow<List<DaySummary>> {
        return dao.observeAllActivePhotos(DateUtils.nowMillis()).map { entities ->
            val photos = entities.map { it.toDomain() }
            val grouped = photos.groupBy { it.dayKey }
            val today = LocalDate.parse(DateUtils.todayDayKey())
            val earliest = grouped.keys.minOfOrNull { LocalDate.parse(it) } ?: today

            generateSequence(today) { current ->
                if (current.minusDays(1).isBefore(earliest)) null else current.minusDays(1)
            }
                .toList()
                .map { date ->
                    val key = date.toString()
                    DaySummary(
                        dayKey = key,
                        photos = grouped[key].orEmpty().sortedByDescending { it.createdAt }
                    )
                }
        }
    }

    override fun observeMemory(memoryId: Long): Flow<MemoryPhoto?> {
        return dao.observeMemoryById(memoryId, DateUtils.nowMillis()).map { it?.toDomain() }
    }

    override suspend fun savePhoto(
        imagePath: String,
        emotionTag: EmotionTag,
        caption: String,
        isTemporary: Boolean
    ) {
        val createdAt = DateUtils.nowMillis()
        val settings = settings.first()
        val expiresAt = if (isTemporary) {
            createdAt + settings.temporaryDurationDays.coerceIn(1, 30) * 24L * 60L * 60L * 1000L
        } else {
            null
        }

        val photo = MemoryPhoto(
            imagePath = imagePath,
            createdAt = createdAt,
            dayKey = DateUtils.dayKeyFromMillis(createdAt),
            emotionTag = emotionTag,
            caption = caption.trim(),
            isTemporary = isTemporary,
            expiresAt = expiresAt,
            isProofPhoto = true
        )

        dao.insert(photo.toEntity())
    }

    override suspend fun updateMemory(
        memoryId: Long,
        emotionTag: EmotionTag,
        caption: String,
        isTemporary: Boolean
    ): Boolean {
        val existing = dao.getMemoryById(memoryId) ?: return false
        val settings = settings.first()
        val expiresAt = when {
            !isTemporary -> null
            existing.isTemporary && existing.expiresAt != null && existing.expiresAt > DateUtils.nowMillis() -> existing.expiresAt
            else -> DateUtils.nowMillis() + settings.temporaryDurationDays.coerceIn(1, 30) * 24L * 60L * 60L * 1000L
        }

        val updated = dao.updateMemory(
            id = memoryId,
            emotionTag = emotionTag.name,
            caption = caption.trim(),
            isTemporary = isTemporary,
            expiresAt = expiresAt
        )
        return updated > 0
    }

    override suspend fun deleteMemory(memoryId: Long): Boolean {
        val existing = dao.getMemoryById(memoryId) ?: return false
        val deleted = dao.deleteById(memoryId) > 0
        if (deleted) {
            runCatching {
                val file = File(existing.imagePath)
                if (file.exists() && file.isFile) {
                    file.delete()
                }
            }
        }
        return deleted
    }

    override suspend fun deleteExpiredPhotos(): Int {
        return dao.deleteExpired(DateUtils.nowMillis())
    }

    override suspend fun setRandomPromptEnabled(enabled: Boolean) {
        settingsPreferences.setRandomPromptEnabled(enabled)
    }

    override suspend fun setRandomVideoChallengeEnabled(enabled: Boolean) {
        settingsPreferences.setRandomVideoChallengeEnabled(enabled)
    }

    override suspend fun setTemporaryDurationDays(days: Int) {
        settingsPreferences.setTemporaryDurationDays(days)
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        settingsPreferences.setDarkModeEnabled(enabled)
    }

    override suspend fun setDisplayName(name: String) {
        settingsPreferences.setDisplayName(name)
    }

    override suspend fun setProfileTagline(tagline: String) {
        settingsPreferences.setProfileTagline(tagline)
    }

    override suspend fun setProfilePhotoUri(uri: String) {
        settingsPreferences.setProfilePhotoUri(uri)
    }
}
