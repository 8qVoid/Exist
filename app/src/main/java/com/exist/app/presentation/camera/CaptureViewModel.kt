package com.exist.app.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.domain.model.EmotionTag
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val capturedPath: String? = null,
    val selectedEmotion: EmotionTag = EmotionTag.NEUTRAL,
    val caption: String = "",
    val temporaryMemory: Boolean = false,
    val randomVideoChallengeEnabled: Boolean = false,
    val challengeRequired: Boolean = false,
    val challengeCompleted: Boolean = true,
    val challengeVideoPath: String? = null,
    val savedCountInSession: Int = 0,
    val saveMessage: String = "",
    val isSaving: Boolean = false,
    val navigateBackSignal: Int = 0
)

class CaptureViewModel(
    private val repository: MemoryRepository
) : ViewModel() {

    private val captureState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = combine(
        captureState,
        repository.settings
    ) { state, settings ->
        state.copy(randomVideoChallengeEnabled = settings.randomVideoChallengeEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CaptureUiState()
    )

    fun onPhotoCaptured(path: String) {
        captureState.update { it.copy(capturedPath = path, saveMessage = "") }
    }

    fun onEmotionSelected(emotionTag: EmotionTag) {
        captureState.update { it.copy(selectedEmotion = emotionTag) }
    }

    fun onCaptionChanged(value: String) {
        captureState.update { it.copy(caption = value.take(160)) }
    }

    fun onTemporaryToggled(enabled: Boolean) {
        captureState.update { it.copy(temporaryMemory = enabled) }
    }

    fun startCameraSession(videoChallengeEnabled: Boolean) {
        captureState.update { current ->
            val requireChallenge = videoChallengeEnabled && (0..99).random() < 35
            current.copy(
                challengeRequired = requireChallenge,
                challengeCompleted = !requireChallenge,
                challengeVideoPath = null,
                saveMessage = ""
            )
        }
    }

    fun onChallengeVideoRecorded(path: String) {
        captureState.update {
            it.copy(
                challengeCompleted = true,
                challengeVideoPath = path,
                saveMessage = "10-second challenge completed."
            )
        }
    }

    fun clearCapturePreview() {
        captureState.update { it.copy(capturedPath = null) }
    }

    fun savePhotoAndContinue() {
        savePhoto(leaveCamera = false)
    }

    fun savePhotoAndFinish() {
        savePhoto(leaveCamera = true)
    }

    private fun savePhoto(leaveCamera: Boolean) {
        val currentState = captureState.value
        val path = currentState.capturedPath ?: return
        if (currentState.challengeRequired && !currentState.challengeCompleted) {
            captureState.update { it.copy(saveMessage = "Record the 10-second challenge clip first.") }
            return
        }

        viewModelScope.launch {
            captureState.update { it.copy(isSaving = true, saveMessage = "") }

            repository.savePhoto(
                imagePath = path,
                emotionTag = captureState.value.selectedEmotion,
                caption = captureState.value.caption,
                isTemporary = captureState.value.temporaryMemory
            )

            val previous = captureState.value
            captureState.update {
                it.copy(
                    capturedPath = null,
                    caption = "",
                    selectedEmotion = EmotionTag.NEUTRAL,
                    temporaryMemory = false,
                    isSaving = false,
                    savedCountInSession = previous.savedCountInSession + 1,
                    saveMessage = if (leaveCamera) "" else "Saved. Capture another moment.",
                    navigateBackSignal = if (leaveCamera) previous.navigateBackSignal + 1 else previous.navigateBackSignal
                )
            }
        }
    }

    fun consumeNavigateBackSignal() {
        captureState.update { it.copy(saveMessage = "") }
    }
}
