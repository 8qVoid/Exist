package com.exist.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MemoryPhotoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ExistDatabase : RoomDatabase() {
    abstract fun memoryPhotoDao(): MemoryPhotoDao
}
