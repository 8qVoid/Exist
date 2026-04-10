package com.exist.app.presentation.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.MemoryPhoto
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecapUiState(
    val photos: List<MemoryPhoto> = emptyList(),
    val frameIndex: Int = 0
)

class RecapViewModel(
    repository: MemoryRepository,
    dayKey: String
) : ViewModel() {

    val uiState: StateFlow<RecapUiState> = repository.observePhotosForDay(dayKey)
        .map { photos -> RecapUiState(photos = photos.sortedBy { it.createdAt }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecapUiState())
}
