package com.exist.app.presentation.highlights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.DaySummary
import com.exist.app.domain.model.MemoryPhoto
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HighlightsUiState(
    val latestMoments: List<MemoryPhoto> = emptyList(),
    val activeDays: List<DaySummary> = emptyList()
)

class HighlightsViewModel(repository: MemoryRepository) : ViewModel() {
    val uiState: StateFlow<HighlightsUiState> = combine(
        repository.observeTodayPhotos(),
        repository.observeTimeline()
    ) { todayPhotos, timeline ->
        val filledDays = timeline.filter { it.photos.isNotEmpty() }
        HighlightsUiState(
            latestMoments = todayPhotos.sortedByDescending { it.createdAt },
            activeDays = filledDays.take(12)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HighlightsUiState()
    )
}
