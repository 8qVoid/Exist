package com.exist.app.domain.auth

data class AuthSession(
    val accessToken: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val needsOnboarding: Boolean = false,
    val isAuthenticated: Boolean = false
)
