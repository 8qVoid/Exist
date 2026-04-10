package com.exist.app.data.auth

import com.exist.app.domain.auth.AuthRepository
import com.exist.app.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AuthRepositoryImpl(
    private val preferences: AuthPreferences
) : AuthRepository {

    override val session: Flow<AuthSession> = preferences.session

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        return runCatching {
            if (!cleanEmail.contains("@")) error("Invalid email")
            if (password.length < 6) error("Password must be at least 6 characters")
            if (preferences.getAccount(cleanEmail) != null) error("Account already exists")
            preferences.upsertAccount(
                LocalAccount(
                    userId = "u_${UUID.randomUUID()}",
                    email = cleanEmail,
                    password = password,
                    fullName = "",
                    birthday = "",
                    profilePhotoUri = ""
                )
            )
        }
    }

    override suspend fun verifyEmailCode(email: String, code: String): Result<AuthSession> {
        return Result.failure(IllegalStateException("Email verification is disabled"))
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        return Result.failure(IllegalStateException("Email verification is disabled"))
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthSession> {
        val cleanEmail = email.trim().lowercase()
        return runCatching {
            val account = preferences.getAccount(cleanEmail) ?: error("Account not found")
            if (account.password != password) error("Invalid email or password")

            val session = account.toSession()
            preferences.saveSession(session)
            session
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthSession> {
        return Result.failure(IllegalStateException("Google login is currently disabled"))
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return runCatching {
            val exists = preferences.getAccount(email.trim().lowercase()) != null
            if (!exists) error("Account not found")
        }
    }

    override suspend fun confirmPasswordReset(email: String, code: String, newPassword: String): Result<Unit> {
        return runCatching {
            if (newPassword.length < 6) error("Password must be at least 6 characters")
            val changed = preferences.setPassword(email.trim().lowercase(), newPassword)
            if (!changed) error("Account not found")
        }
    }

    override suspend fun completeOnboarding(fullName: String, birthday: String, profilePhotoUri: String): Result<AuthSession> {
        return runCatching {
            val current = preferences.sessionState()
            val account = preferences.getAccount(current.email) ?: error("Account not found")
            val updated = account.copy(
                fullName = fullName.trim(),
                birthday = birthday.trim(),
                profilePhotoUri = profilePhotoUri.trim()
            )
            preferences.upsertAccount(updated)
            val session = updated.toSession()
            preferences.saveSession(session)
            session
        }
    }

    override suspend fun signOut() {
        preferences.clearSessionOnly()
    }

    private fun LocalAccount.toSession(): AuthSession {
        val name = fullName.ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
        val needsOnboarding = fullName.isBlank() || birthday.isBlank()
        return AuthSession(
            accessToken = "local_${userId}",
            userId = userId,
            email = email,
            displayName = name,
            needsOnboarding = needsOnboarding,
            isAuthenticated = true
        )
    }
}
