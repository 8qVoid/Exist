package com.exist.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: MemoryPhotoEntity): Long

    @Query(
        """
        SELECT * FROM memory_photos
        WHERE dayKey = :dayKey
        AND (expiresAt IS NULL OR expiresAt > :now)
        ORDER BY createdAt DESC
        """
    )
    fun observePhotosForDay(dayKey: String, now: Long): Flow<List<MemoryPhotoEntity>>

    @Query(
        """
        SELECT * FROM memory_photos
        WHERE (expiresAt IS NULL OR expiresAt > :now)
        ORDER BY createdAt DESC
        """
    )
    fun observeAllActivePhotos(now: Long): Flow<List<MemoryPhotoEntity>>

    @Query(
        """
        SELECT * FROM memory_photos
        WHERE id = :id
        AND (expiresAt IS NULL OR expiresAt > :now)
        LIMIT 1
        """
    )
    fun observeMemoryById(id: Long, now: Long): Flow<MemoryPhotoEntity?>

    @Query("SELECT * FROM memory_photos WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: Long): MemoryPhotoEntity?

    @Query(
        """
        UPDATE memory_photos
        SET emotionTag = :emotionTag,
            caption = :caption,
            isTemporary = :isTemporary,
            expiresAt = :expiresAt
        WHERE id = :id
        """
    )
    suspend fun updateMemory(
        id: Long,
        emotionTag: String,
        caption: String,
        isTemporary: Boolean,
        expiresAt: Long?
    ): Int

    @Query("DELETE FROM memory_photos WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM memory_photos
            WHERE dayKey = :dayKey
            AND isProofPhoto = 1
            AND (expiresAt IS NULL OR expiresAt > :now)
            LIMIT 1
        )
        """
    )
    fun observeHasProofForDay(dayKey: String, now: Long): Flow<Boolean>

    @Query("DELETE FROM memory_photos WHERE expiresAt IS NOT NULL AND expiresAt <= :now")
    suspend fun deleteExpired(now: Long): Int
}
