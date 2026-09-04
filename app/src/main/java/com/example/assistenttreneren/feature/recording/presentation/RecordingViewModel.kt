package com.example.assistenttreneren.feature.recording.presentation

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.core.media.CameraXVideoRecorder
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.model.RecordingStatus
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSubCategory
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.recording.domain.usecase.GetRecordingStatusUseCase
import com.example.assistenttreneren.feature.recording.domain.usecase.StartRecordingUseCase
import com.example.assistenttreneren.feature.recording.domain.usecase.StopRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecordingViewModel @Inject constructor(
    getRecordingStatusUseCase: GetRecordingStatusUseCase,
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val localRecordingRepository: LocalRecordingRepository,
    private val cameraXVideoRecorder: CameraXVideoRecorder,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecordingStatusUseCase().collectLatest { status ->
                _uiState.update { currentState ->
                    when (status) {
                        RecordingStatus.Idle -> currentState.copy(
                            isRecording = false,
                            recordingStartedAtMillis = null,
                            activeDisplayName = null,
                        )

                        is RecordingStatus.Recording -> currentState.copy(
                            isRecording = true,
                            selectedMediaType = status.mediaType,
                            recordingStartedAtMillis = status.startedAtMillis,
                            activeDisplayName = status.displayName,
                            errorMessage = null,
                        )

                        is RecordingStatus.Completed -> currentState.copy(
                            isRecording = false,
                            recordingStartedAtMillis = null,
                            activeDisplayName = null,
                            completedRecording = status.session,
                            errorMessage = null,
                        ).also {
                            saveCompletedRecording(status.session)
                        }

                        is RecordingStatus.Error -> currentState.copy(
                            isRecording = false,
                            recordingStartedAtMillis = null,
                            activeDisplayName = null,
                            errorMessage = status.message,
                        )
                    }
                }
            }
        }
    }

    fun onSubCategorySelected(subCategory: RecordingSubCategory) {
        _uiState.update { currentState ->
            currentState.copy(subCategory = subCategory.displayName)
        }
    }

    fun onMediaTypeSelected(mediaType: RecordingMediaType) {
        _uiState.update { currentState ->
            if (currentState.isRecording) {
                currentState
            } else {
                currentState.copy(
                    selectedMediaType = mediaType,
                    errorMessage = null,
                )
            }
        }
    }

    fun clearSubCategory() {
        _uiState.update { currentState ->
            currentState.copy(subCategory = "")
        }
    }

    fun startAudioRecording(
        category: String,
        activityId: String?,
        matchPeriod: String? = null,
        matchClockStartMillis: Long? = null,
    ) {
        val subCategory = uiState.value.subCategory.trim()
        if (subCategory.isBlank()) {
            return
        }

        _uiState.update { it.copy(selectedMediaType = RecordingMediaType.Audio) }
        startRecordingUseCase(
            category = category,
            subCategory = subCategory,
            activityId = activityId,
            matchPeriod = matchPeriod,
            matchClockStartMillis = matchClockStartMillis,
        )
    }

    fun bindVideoPreview(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        viewModelScope.launch {
            runCatching {
                cameraXVideoRecorder.bindPreview(
                    context = context.applicationContext,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Kunne ikke starte kamera preview.")
                }
            }
        }
    }

    fun startVideoRecording(
        context: Context,
        category: String,
        activityId: String?,
        matchPeriod: String? = null,
        matchClockStartMillis: Long? = null,
    ) {
        val subCategory = uiState.value.subCategory.trim()
        if (subCategory.isBlank()) {
            return
        }

        _uiState.update {
            it.copy(
                selectedMediaType = RecordingMediaType.Video,
                errorMessage = null,
            )
        }

        runCatching {
            cameraXVideoRecorder.startRecording(
                context = context.applicationContext,
                category = category,
                subCategory = subCategory,
                activityId = activityId,
                matchPeriod = matchPeriod,
                matchClockStartMillis = matchClockStartMillis,
                onStarted = { activeRecording ->
                    _uiState.update {
                        it.copy(
                            isRecording = true,
                            selectedMediaType = RecordingMediaType.Video,
                            recordingStartedAtMillis = activeRecording.startedAtMillis,
                            activeDisplayName = activeRecording.displayName,
                            errorMessage = null,
                        )
                    }
                },
                onCompleted = { session ->
                    saveCompletedRecording(session)
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            recordingStartedAtMillis = null,
                            activeDisplayName = null,
                            completedRecording = session,
                            errorMessage = null,
                        )
                    }
                },
                onError = { throwable ->
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            recordingStartedAtMillis = null,
                            activeDisplayName = null,
                            errorMessage = throwable.message ?: "Kunne ikke lagre videoopptak.",
                        )
                    }
                },
            )
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(errorMessage = throwable.message ?: "Kunne ikke starte videoopptak.")
            }
        }
    }

    fun stopRecording() {
        when (uiState.value.selectedMediaType) {
            RecordingMediaType.Video -> cameraXVideoRecorder.stopRecording()
            RecordingMediaType.Audio,
            null,
            -> stopRecordingUseCase()
        }
    }

    override fun onCleared() {
        cameraXVideoRecorder.cancelRecording()
        super.onCleared()
    }

    private fun saveCompletedRecording(recordingSession: com.example.assistenttreneren.feature.recording.domain.model.RecordingSession) {
        viewModelScope.launch {
            localRecordingRepository.saveRecording(recordingSession)
        }
    }
}
