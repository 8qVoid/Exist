package com.exist.app.domain.auth

interface AuthRepository {
    val session: kotlinx.coroutines.flow.Flow<AuthSession>

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>
    suspend fun verifyEmailCode(email: String, code: String): Result<AuthSession>
    suspend fun resendVerification(email: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthSession>
    suspend fun signInWithGoogle(idToken: String): Result<AuthSession>

    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun confirmPasswordReset(email: String, code: String, newPassword: String): Result<Unit>

    suspend fun completeOnboarding(fullName: String, birthday: String, profilePhotoUri: String): Result<AuthSession>
    suspend fun signOut()
}
