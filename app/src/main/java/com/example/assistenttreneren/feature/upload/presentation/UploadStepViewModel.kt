package com.example.assistenttreneren.feature.upload.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import com.example.assistenttreneren.feature.upload.domain.usecase.EnqueueRecordingUploadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UploadStepViewModel @Inject constructor(
    private val localRecordingRepository: LocalRecordingRepository,
    private val localUploadRepository: LocalUploadRepository,
    private val enqueueRecordingUploadUseCase: EnqueueRecordingUploadUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UploadStepUiState())
    val uiState: StateFlow<UploadStepUiState> = _uiState.asStateFlow()

    private var activityCollectionJob: Job? = null

    fun loadActivity(activityId: String?) {
        if (activityId == null || activityId == uiState.value.activityId) {
            return
        }

        activityCollectionJob?.cancel()
        activityCollectionJob = viewModelScope.launch {
            combine(
                localRecordingRepository.observeRecordingsForActivity(activityId),
                localUploadRepository.observeUploadJobsForActivity(activityId),
            ) { recordings, uploadJobs ->
                buildUiState(
                    activityId = activityId,
                    recordings = recordings,
                    uploadJobs = uploadJobs,
                    selectedRecordingId = uiState.value.selectedRecordingId,
                    errorMessage = uiState.value.errorMessage,
                )
            }.collect { nextState ->
                _uiState.value = nextState
            }
        }
    }

    fun onRecordingSelected(recordingId: String) {
        _uiState.update {
            it.copy(
                selectedRecordingId = recordingId,
                errorMessage = null,
            )
        }
    }

    fun uploadSelectedRecording() {
        val recordingId = uiState.value.selectedRecordingId ?: return

        viewModelScope.launch {
            val recording = localRecordingRepository.getRecording(recordingId)
            if (recording == null) {
                _uiState.update { it.copy(errorMessage = "Opptaket finnes ikke lokalt.") }
                return@launch
            }

            enqueueRecordingUploadUseCase(recording)
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun retryUpload(uploadJobId: String) {
        viewModelScope.launch {
            val uploadJob = localUploadRepository.getUploadJob(uploadJobId)
            if (uploadJob == null) {
                _uiState.update { it.copy(errorMessage = "Upload-jobben finnes ikke.") }
                return@launch
            }

            localUploadRepository.saveUploadJob(
                uploadJob.copy(
                    status = UploadStatus.Queued,
                    statusMessage = "Opptaket er satt i kø.",
                    progressPercent = uploadJob.progressPercent ?: 0,
                    updatedAtMillis = System.currentTimeMillis(),
                    lastError = null,
                ),
            )
            enqueueRecordingUploadUseCase.retry(uploadJob)
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun buildUiState(
        activityId: String,
        recordings: List<RecordingSession>,
        uploadJobs: List<UploadJob>,
        selectedRecordingId: String?,
        errorMessage: String?,
    ): UploadStepUiState {
        val jobsByRecordingId = uploadJobs.groupBy { it.recordingId }
        val recordingNames = recordings.associate { it.recordingId to it.displayName }
        val recordingMediaTypes = recordings.associate { it.recordingId to it.mediaType }
        val recordingUiModels = recordings.map { recording ->
            UploadRecordingUiModel(
                recordingId = recording.recordingId,
                displayName = recording.displayName,
                mediaType = recording.mediaType,
                mimeType = recording.mimeType,
                durationMillis = recording.durationMillis,
                category = recording.category,
                subCategory = recording.subCategory,
                createdAtMillis = recording.createdAtMillis,
                hasActiveUpload = jobsByRecordingId[recording.recordingId]
                    .orEmpty()
                    .any { it.status.isActive },
            )
        }
        val validSelectedRecordingId = selectedRecordingId
            ?.takeIf { id -> recordingUiModels.any { it.recordingId == id } }

        return UploadStepUiState(
            activityId = activityId,
            recordings = recordingUiModels,
            uploadJobs = uploadJobs.map { uploadJob ->
                UploadJobUiModel(
                    uploadJobId = uploadJob.uploadJobId,
                    recordingId = uploadJob.recordingId,
                    recordingDisplayName = recordingNames[uploadJob.recordingId]
                        ?: uploadJob.recordingId,
                    mediaType = recordingMediaTypes[uploadJob.recordingId] ?: RecordingMediaType.Audio,
                    status = uploadJob.status,
                    statusMessage = uploadJob.statusMessage,
                    progressPercent = uploadJob.progressPercent,
                    lastError = uploadJob.lastError,
                )
            },
            selectedRecordingId = validSelectedRecordingId,
            errorMessage = errorMessage,
        )
    }

    private val UploadStatus.isActive: Boolean
        get() = when (this) {
            UploadStatus.Queued,
            UploadStatus.Uploading,
            UploadStatus.ProcessingAudio,
            UploadStatus.Transcribing,
            -> true

            UploadStatus.Completed,
            UploadStatus.Failed,
            -> false
        }
}
