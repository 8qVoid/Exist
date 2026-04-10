package com.exist.app.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.DaySummary
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ArchiveUiState(
    val days: List<DaySummary> = emptyList()
)

class ArchiveViewModel(repository: MemoryRepository) : ViewModel() {
    val uiState: StateFlow<ArchiveUiState> = repository.observeTimeline()
        .map { ArchiveUiState(days = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArchiveUiState())
}
