package com.exist.app.core

import android.content.Context
import androidx.room.Room
import com.exist.app.data.auth.AuthPreferences
import com.exist.app.data.auth.AuthRepositoryImpl
import com.exist.app.data.local.ExistDatabase
import com.exist.app.data.local.MIGRATION_1_2
import com.exist.app.data.preferences.SettingsPreferences
import com.exist.app.data.repository.MemoryRepositoryImpl
import com.exist.app.domain.auth.AuthRepository
import com.exist.app.domain.repository.MemoryRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: ExistDatabase = Room.databaseBuilder(
        appContext,
        ExistDatabase::class.java,
        "exist.db"
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    private val settingsPreferences = SettingsPreferences(appContext)

    private val authPreferences = AuthPreferences(appContext)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        preferences = authPreferences
    )

    val memoryRepository: MemoryRepository = MemoryRepositoryImpl(
        dao = database.memoryPhotoDao(),
        settingsPreferences = settingsPreferences
    )
}
