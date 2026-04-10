package com.exist.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.MemoryPhoto
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val todayHasProof: Boolean = false,
    val todayPhotos: List<MemoryPhoto> = emptyList(),
    val displayName: String = "You",
    val profileTagline: String = ""
)

class HomeViewModel(
    repository: MemoryRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeTodayProofStatus(),
        repository.observeTodayPhotos(),
        repository.settings
    ) { hasProof, photos, settings ->
        HomeUiState(
            todayHasProof = hasProof,
            todayPhotos = photos,
            displayName = settings.displayName,
            profileTagline = settings.profileTagline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
