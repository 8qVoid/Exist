package com.exist.app.presentation.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.auth.AuthRepository
import com.exist.app.domain.auth.AuthSession
import com.exist.app.domain.model.AppSettings
import com.exist.app.domain.repository.MemoryRepository
import com.exist.app.notifications.RandomPromptScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository,
    private val app: Application
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val authSession: StateFlow<AuthSession> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthSession())

    fun setRandomPrompt(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRandomPromptEnabled(enabled)
            RandomPromptScheduler.sync(app, enabled)
        }
    }

    fun setRandomVideoChallenge(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRandomVideoChallengeEnabled(enabled)
        }
    }

    fun setTemporaryDuration(days: Int) {
        viewModelScope.launch {
            repository.setTemporaryDurationDays(days)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkModeEnabled(enabled)
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            repository.setDisplayName(name)
        }
    }

    fun setProfileTagline(tagline: String) {
        viewModelScope.launch {
            repository.setProfileTagline(tagline)
        }
    }

    fun setProfilePhotoUri(uri: String) {
        viewModelScope.launch {
            repository.setProfilePhotoUri(uri)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
