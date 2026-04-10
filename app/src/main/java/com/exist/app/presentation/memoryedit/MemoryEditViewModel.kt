package com.exist.app.presentation.memoryedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.EmotionTag
import com.exist.app.domain.model.MemoryPhoto
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryEditUiState(
    val memory: MemoryPhoto? = null,
    val caption: String = "",
    val emotionTag: EmotionTag = EmotionTag.NEUTRAL,
    val isTemporary: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleted: Boolean = false,
    val saveCompleted: Boolean = false
)

class MemoryEditViewModel(
    private val repository: MemoryRepository,
    memoryId: Long
) : ViewModel() {

    private val editorState = MutableStateFlow(MemoryEditUiState())

    val uiState: StateFlow<MemoryEditUiState> = combine(
        repository.observeMemory(memoryId),
        editorState
    ) { memory, editor ->
        if (memory == null) {
            editor.copy(memory = null)
        } else if (editor.memory?.id != memory.id && !editor.isSaving) {
            editor.copy(
                memory = memory,
                caption = memory.caption,
                emotionTag = memory.emotionTag,
                isTemporary = memory.isTemporary
            )
        } else {
            editor.copy(memory = memory)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemoryEditUiState())

    fun onCaptionChanged(value: String) {
        editorState.update { it.copy(caption = value.take(160), saveCompleted = false) }
    }

    fun onEmotionSelected(tag: EmotionTag) {
        editorState.update { it.copy(emotionTag = tag, saveCompleted = false) }
    }

    fun onTemporaryChanged(enabled: Boolean) {
        editorState.update { it.copy(isTemporary = enabled, saveCompleted = false) }
    }

    fun saveChanges() {
        val memory = uiState.value.memory ?: return
        viewModelScope.launch {
            editorState.update { it.copy(isSaving = true) }
            repository.updateMemory(
                memoryId = memory.id,
                emotionTag = uiState.value.emotionTag,
                caption = uiState.value.caption,
                isTemporary = uiState.value.isTemporary
            )
            editorState.update { it.copy(isSaving = false, saveCompleted = true) }
        }
    }

    fun deleteMemory() {
        val memory = uiState.value.memory ?: return
        viewModelScope.launch {
            repository.deleteMemory(memory.id)
            editorState.update { it.copy(isDeleted = true) }
        }
    }

    fun consumeSaveComplete() {
        editorState.update { it.copy(saveCompleted = false) }
    }
}
