package com.exist.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.exist.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.existDataStore by preferencesDataStore(name = "exist_settings")

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val randomPromptEnabled = booleanPreferencesKey("random_prompt_enabled")
        val randomVideoChallengeEnabled = booleanPreferencesKey("random_video_challenge_enabled")
        val temporaryDurationDays = intPreferencesKey("temporary_duration_days")
        val darkModeEnabled = booleanPreferencesKey("dark_mode_enabled")
        val displayName = stringPreferencesKey("display_name")
        val profileTagline = stringPreferencesKey("profile_tagline")
        val profilePhotoUri = stringPreferencesKey("profile_photo_uri")
    }

    val settings: Flow<AppSettings> = context.existDataStore.data.map { prefs: Preferences ->
        AppSettings(
            randomPromptEnabled = prefs[Keys.randomPromptEnabled] ?: false,
            randomVideoChallengeEnabled = prefs[Keys.randomVideoChallengeEnabled] ?: false,
            temporaryDurationDays = (prefs[Keys.temporaryDurationDays] ?: 7).coerceIn(1, 30),
            darkModeEnabled = prefs[Keys.darkModeEnabled] ?: true,
            displayName = (prefs[Keys.displayName] ?: "You").ifBlank { "You" },
            profileTagline = prefs[Keys.profileTagline] ?: "",
            profilePhotoUri = prefs[Keys.profilePhotoUri] ?: ""
        )
    }

    suspend fun setRandomPromptEnabled(enabled: Boolean) {
        context.existDataStore.edit { it[Keys.randomPromptEnabled] = enabled }
    }

    suspend fun setRandomVideoChallengeEnabled(enabled: Boolean) {
        context.existDataStore.edit { it[Keys.randomVideoChallengeEnabled] = enabled }
    }

    suspend fun setTemporaryDurationDays(days: Int) {
        context.existDataStore.edit { it[Keys.temporaryDurationDays] = days.coerceIn(1, 30) }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.existDataStore.edit { it[Keys.darkModeEnabled] = enabled }
    }

    suspend fun setDisplayName(name: String) {
        context.existDataStore.edit { it[Keys.displayName] = name.trim().ifBlank { "You" } }
    }

    suspend fun setProfileTagline(tagline: String) {
        context.existDataStore.edit { it[Keys.profileTagline] = tagline.trim() }
    }

    suspend fun setProfilePhotoUri(uri: String) {
        context.existDataStore.edit { it[Keys.profilePhotoUri] = uri.trim() }
    }
}
