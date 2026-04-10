package com.exist.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.auth.AuthRepository
import com.exist.app.domain.auth.AuthSession
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { SIGN_IN, SIGN_UP }
enum class AuthStep { CREDENTIALS, ONBOARDING }

data class AuthUiState(
    val session: AuthSession = AuthSession(),
    val mode: AuthMode = AuthMode.SIGN_IN,
    val step: AuthStep = AuthStep.CREDENTIALS,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val code: String = "",
    val resetPassword: String = "",
    val fullName: String = "",
    val birthday: String = "",
    val profilePhotoUri: String = "",
    val isLoading: Boolean = false,
    val cooldownSeconds: Int = 0,
    val message: String = "",
    val error: String = ""
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                _uiState.update { current ->
                    val step = if (session.isAuthenticated && session.needsOnboarding) AuthStep.ONBOARDING else current.step
                    current.copy(session = session, step = step)
                }
            }
        }
    }

    fun switchMode(mode: AuthMode) {
        _uiState.update {
            it.copy(mode = mode, step = AuthStep.CREDENTIALS, error = "", message = "", password = "", confirmPassword = "", code = "")
        }
    }

    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value.trim(), error = "") }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, error = "") }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, error = "") }
    fun onFullNameChanged(value: String) = _uiState.update { it.copy(fullName = value.take(60), error = "") }
    fun onProfilePhotoUriChanged(value: String) = _uiState.update { it.copy(profilePhotoUri = value, error = "") }

    fun submitCredentials() {
        val email = uiState.value.email
        if (!email.contains("@")) {
            _uiState.update { it.copy(error = "Enter a valid email") }
            return
        }
        if (uiState.value.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "", message = "") }
            if (uiState.value.mode == AuthMode.SIGN_UP) {
                if (uiState.value.password != uiState.value.confirmPassword) {
                    _uiState.update { it.copy(isLoading = false, error = "Passwords do not match") }
                    return@launch
                }
                val result = authRepository.signUpWithEmail(email, uiState.value.password)
                if (result.isSuccess) {
                    val signInResult = authRepository.signInWithEmail(email, uiState.value.password)
                    if (signInResult.isSuccess) {
                        val session = signInResult.getOrThrow()
                        memoryRepository.setDisplayName(session.displayName)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                step = if (session.needsOnboarding) AuthStep.ONBOARDING else AuthStep.CREDENTIALS,
                                message = "Account created"
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = signInResult.exceptionOrNull()?.message ?: "Signup failed") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Signup failed") }
                }
            } else {
                val result = authRepository.signInWithEmail(email, uiState.value.password)
                if (result.isSuccess) {
                    val session = result.getOrThrow()
                    memoryRepository.setDisplayName(session.displayName)
                    if (session.needsOnboarding) {
                        _uiState.update { it.copy(isLoading = false, step = AuthStep.ONBOARDING, message = "Complete your profile") }
                    } else {
                        _uiState.update { it.copy(isLoading = false, message = "Welcome back") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Login failed") }
                }
            }
        }
    }

    fun completeOnboarding() {
        val fullName = uiState.value.fullName.trim()
        val birthday = uiState.value.birthday.trim()
        val birthdayRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
        if (fullName.isBlank() || !birthdayRegex.matches(birthday)) {
            _uiState.update { it.copy(error = "Use full name and birthday format YYYY-MM-DD") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            val result = authRepository.completeOnboarding(fullName, birthday, uiState.value.profilePhotoUri)
            if (result.isSuccess) {
                memoryRepository.setDisplayName(fullName)
                memoryRepository.setProfilePhotoUri(uiState.value.profilePhotoUri)
                _uiState.update { it.copy(isLoading = false, message = "Profile completed") }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Onboarding failed") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update {
                it.copy(
                    step = AuthStep.CREDENTIALS,
                    mode = AuthMode.SIGN_IN,
                    password = "",
                    confirmPassword = "",
                    code = "",
                    resetPassword = "",
                    message = "Signed out"
                )
            }
        }
    }

    fun onBirthdayChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(8)
        val formatted = buildString {
            for ((index, c) in digits.withIndex()) {
                append(c)
                if (index == 3 || index == 5) append('-')
            }
        }.take(10)
        _uiState.update { it.copy(birthday = formatted, error = "") }
    }
}
