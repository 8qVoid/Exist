package com.exist.app.presentation.daydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.MemoryPhoto
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayDetailUiState(
    val photos: List<MemoryPhoto> = emptyList()
)

class DayDetailViewModel(
    private val repository: MemoryRepository,
    dayKey: String
) : ViewModel() {

    val uiState: StateFlow<DayDetailUiState> = repository.observePhotosForDay(dayKey)
        .map { photos -> DayDetailUiState(photos = photos) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DayDetailUiState()
        )

    fun deleteMemory(memoryId: Long) {
        viewModelScope.launch {
            repository.deleteMemory(memoryId)
        }
    }
}
