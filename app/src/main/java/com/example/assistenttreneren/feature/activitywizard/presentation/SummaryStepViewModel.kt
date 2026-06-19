package com.example.assistenttreneren.feature.activitywizard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class SummaryStepViewModel @Inject constructor(
    private val localRecordingRepository: LocalRecordingRepository,
    private val localUploadRepository: LocalUploadRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SummaryStepUiState())
    val uiState: StateFlow<SummaryStepUiState> = _uiState.asStateFlow()

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
                )
            }.collect { nextState ->
                _uiState.value = nextState
            }
        }
    }

    private fun buildUiState(
        activityId: String,
        recordings: List<RecordingSession>,
        uploadJobs: List<UploadJob>,
    ): SummaryStepUiState {
        val recordingNames = recordings.associate { it.recordingId to it.displayName }
        val recordingMediaTypes = recordings.associate { it.recordingId to it.mediaType }

        return SummaryStepUiState(
            activityId = activityId,
            recordings = recordings.map { recording ->
                SummaryRecordingUiModel(
                    recordingId = recording.recordingId,
                    displayName = recording.displayName,
                    mediaType = recording.mediaType,
                    durationMillis = recording.durationMillis,
                    subCategory = recording.subCategory,
                )
            },
            uploadJobs = uploadJobs.map { uploadJob ->
                SummaryUploadJobUiModel(
                    uploadJobId = uploadJob.uploadJobId,
                    recordingDisplayName = recordingNames[uploadJob.recordingId]
                        ?: uploadJob.recordingId,
                    mediaType = recordingMediaTypes[uploadJob.recordingId]
                        ?: RecordingMediaType.Audio,
                    status = uploadJob.status,
                    statusMessage = uploadJob.statusMessage,
                    progressPercent = uploadJob.progressPercent,
                )
            },
        )
    }
}
