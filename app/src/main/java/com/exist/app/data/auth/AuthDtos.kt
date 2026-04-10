package com.exist.app.data.auth

data class EmailBody(val email: String)
data class EmailPasswordBody(val email: String, val password: String)
data class VerifyEmailBody(val email: String, val code: String)
data class GoogleSignInBody(val idToken: String)
data class ResetPasswordBody(val email: String, val code: String, val newPassword: String)
data class OnboardingBody(val fullName: String, val birthday: String, val profilePhotoUri: String)

data class AuthResponse(
    val accessToken: String,
    val userId: String,
    val email: String,
    val displayName: String?,
    val needsOnboarding: Boolean?
)
