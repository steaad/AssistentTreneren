package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionReview
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

data class SummaryStepUiState(
    val activityId: String? = null,
    val recordings: List<SummaryRecordingUiModel> = emptyList(),
    val uploadJobs: List<SummaryUploadJobUiModel> = emptyList(),
    val transcriptionReview: TranscriptionReview? = null,
    val isLoadingTranscriptionReview: Boolean = false,
    val transcriptionErrorMessage: String? = null,
    val isSubmittingTranscriptionAction: Boolean = false,
) {
    val audioRecordingCount get() = recordings.count { it.mediaType == RecordingMediaType.Audio }
    val videoRecordingCount get() = recordings.count { it.mediaType == RecordingMediaType.Video }
    val completedUploadCount get() = uploadJobs.count { it.status == UploadStatus.Completed }
    val failedUploadCount get() = uploadJobs.count { it.status == UploadStatus.Failed }
    val processingAudioCount get() = uploadJobs.count { it.mediaType == RecordingMediaType.Audio && it.status in setOf(UploadStatus.Uploading, UploadStatus.Queued, UploadStatus.ProcessingAudio, UploadStatus.Transcribing) }
    val readyTranscriptionCount get() = transcriptionReview?.recordings?.size ?: 0
    val pendingIssueCount get() = transcriptionReview?.recordings?.sumOf { it.issues.size } ?: 0
    val failedAudioCount get() = uploadJobs.count { it.mediaType == RecordingMediaType.Audio && it.status == UploadStatus.Failed }
}

data class SummaryRecordingUiModel(val recordingId: String, val displayName: String, val mediaType: RecordingMediaType, val durationMillis: Long, val subCategory: String)
data class SummaryUploadJobUiModel(val uploadJobId: String, val recordingDisplayName: String, val mediaType: RecordingMediaType, val status: UploadStatus, val statusMessage: String?, val progressPercent: Int?)
