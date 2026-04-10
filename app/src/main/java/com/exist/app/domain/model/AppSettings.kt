package com.exist.app.domain.model

data class AppSettings(
    val randomPromptEnabled: Boolean = false,
    val randomVideoChallengeEnabled: Boolean = false,
    val temporaryDurationDays: Int = 7,
    val darkModeEnabled: Boolean = true,
    val displayName: String = "You",
    val profileTagline: String = "",
    val profilePhotoUri: String = ""
)
