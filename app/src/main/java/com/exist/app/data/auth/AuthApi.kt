package com.exist.app.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/signup")
    suspend fun signUp(@Body body: EmailPasswordBody): Response<Unit>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailBody): Response<AuthResponse>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: EmailBody): Response<Unit>

    @POST("auth/login")
    suspend fun login(@Body body: EmailPasswordBody): Response<AuthResponse>

    @POST("auth/google")
    suspend fun signInWithGoogle(@Body body: GoogleSignInBody): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: EmailBody): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordBody): Response<Unit>

    @POST("auth/onboarding")
    suspend fun completeOnboarding(@Body body: OnboardingBody): Response<AuthResponse>

    @POST("auth/signout")
    suspend fun signOut(): Response<Unit>
}
