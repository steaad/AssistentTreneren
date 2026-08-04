package com.example.assistenttreneren.feature.activitywizard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewError
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewResult
import com.example.assistenttreneren.feature.transcription.domain.usecase.DeleteTranscriptionEventUseCase
import com.example.assistenttreneren.feature.transcription.domain.usecase.DismissTranscriptionIssueUseCase
import com.example.assistenttreneren.feature.transcription.domain.usecase.GetTranscriptionReviewUseCase
import com.example.assistenttreneren.feature.transcription.domain.usecase.ResolveTranscriptionIssueUseCase
import com.example.assistenttreneren.feature.transcription.domain.usecase.UpdateTranscriptionEventUseCase
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SummaryStepViewModel @Inject constructor(
    private val localRecordingRepository: LocalRecordingRepository,
    private val localUploadRepository: LocalUploadRepository,
    private val getReview: GetTranscriptionReviewUseCase,
    private val resolveTranscriptionIssueUseCase: ResolveTranscriptionIssueUseCase,
    private val dismissTranscriptionIssueUseCase: DismissTranscriptionIssueUseCase,
    private val updateTranscriptionEventUseCase: UpdateTranscriptionEventUseCase,
    private val deleteTranscriptionEventUseCase: DeleteTranscriptionEventUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SummaryStepUiState())
    val uiState = _uiState.asStateFlow()
    private var collectionJob: Job? = null
    private var completedAudioIds = emptySet<String>()

    fun loadActivity(activityId: String?) {
        if (activityId == null || activityId == _uiState.value.activityId) return
        collectionJob?.cancel()
        completedAudioIds = emptySet()
        _uiState.update { it.copy(activityId = activityId, transcriptionReview = null, transcriptionErrorMessage = null) }
        collectionJob = viewModelScope.launch {
            combine(localRecordingRepository.observeRecordingsForActivity(activityId), localUploadRepository.observeUploadJobsForActivity(activityId)) { recordings, jobs -> recordings to jobs }
                .collect { (recordings, jobs) ->
                    val names = recordings.associate { it.recordingId to it.displayName }
                    val types = recordings.associate { it.recordingId to it.mediaType }
                    _uiState.update { it.copy(activityId = activityId, recordings = recordings.map { r -> SummaryRecordingUiModel(r.recordingId, r.displayName, r.mediaType, r.durationMillis, r.subCategory) }, uploadJobs = jobs.map { j -> SummaryUploadJobUiModel(j.uploadJobId, names[j.recordingId] ?: j.recordingId, types[j.recordingId] ?: RecordingMediaType.Audio, j.status, j.statusMessage, j.progressPercent) }) }
                    val nextCompleted = jobs.filter { it.status.name == "Completed" && types[it.recordingId] == RecordingMediaType.Audio }.map { it.recordingId }.toSet()
                    if (nextCompleted != completedAudioIds) { completedAudioIds = nextCompleted; refreshTranscriptionReview() }
                }
        }
        refreshTranscriptionReview()
    }

    fun refreshTranscriptionReview() {
        val activityId = _uiState.value.activityId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTranscriptionReview = true, transcriptionErrorMessage = null) }
            when (val result = getReview(activityId)) {
                is TranscriptionReviewResult.Success -> _uiState.update { it.copy(transcriptionReview = result.data, isLoadingTranscriptionReview = false) }
                is TranscriptionReviewResult.Failure -> _uiState.update { it.copy(isLoadingTranscriptionReview = false, transcriptionErrorMessage = result.error.message()) }
            }
        }
    }

    fun resolveIssue(issueId: String, input: TranscriptionEventInput) = submit { resolveTranscriptionIssueUseCase(issueId, input) }
    fun dismissIssue(issueId: String) = submit { dismissTranscriptionIssueUseCase(issueId) }
    fun updateEvent(eventId: String, input: TranscriptionEventInput) = submit { updateTranscriptionEventUseCase(eventId, input) }
    fun deleteEvent(eventId: String) = submit { deleteTranscriptionEventUseCase(eventId) }

    private fun submit(action: suspend () -> TranscriptionReviewResult<Unit>) = viewModelScope.launch {
        _uiState.update { it.copy(isSubmittingTranscriptionAction = true, transcriptionErrorMessage = null) }
        when (val result = action()) {
            is TranscriptionReviewResult.Success -> { _uiState.update { it.copy(isSubmittingTranscriptionAction = false) }; refreshTranscriptionReview() }
            is TranscriptionReviewResult.Failure -> _uiState.update { it.copy(isSubmittingTranscriptionAction = false, transcriptionErrorMessage = result.error.message()) }
        }
    }

    private fun TranscriptionReviewError.message() = when (this) {
        TranscriptionReviewError.InvalidInput -> "Kontroller tekst og tidsrom."
        TranscriptionReviewError.InvalidServerResponse -> "Ugyldig svar fra serveren."
        TranscriptionReviewError.NetworkUnavailable -> "Ingen nettverkstilkobling."
        TranscriptionReviewError.NotFound -> "Elementet finnes ikke lenger."
        TranscriptionReviewError.Unauthorized -> "Du må logge inn på nytt."
        is TranscriptionReviewError.ServerError -> "Serverfeil ($code)."
        is TranscriptionReviewError.Unexpected -> "Noe gikk galt."
    }
}
