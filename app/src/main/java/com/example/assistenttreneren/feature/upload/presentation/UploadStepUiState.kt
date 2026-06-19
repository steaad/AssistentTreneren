package com.example.assistenttreneren.feature.upload.presentation

import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

data class UploadStepUiState(
    val activityId: String? = null,
    val recordings: List<UploadRecordingUiModel> = emptyList(),
    val uploadJobs: List<UploadJobUiModel> = emptyList(),
    val selectedRecordingId: String? = null,
    val errorMessage: String? = null,
) {
    val selectedRecording: UploadRecordingUiModel?
        get() = recordings.firstOrNull { it.recordingId == selectedRecordingId }

    val canUploadSelectedRecording: Boolean
        get() = activityId != null &&
            selectedRecording != null &&
            selectedRecording?.hasActiveUpload != true
}

data class UploadRecordingUiModel(
    val recordingId: String,
    val displayName: String,
    val mediaType: RecordingMediaType,
    val mimeType: String,
    val durationMillis: Long,
    val category: String,
    val subCategory: String,
    val createdAtMillis: Long,
    val hasActiveUpload: Boolean,
)

data class UploadJobUiModel(
    val uploadJobId: String,
    val recordingId: String,
    val recordingDisplayName: String,
    val status: UploadStatus,
    val statusMessage: String?,
    val progressPercent: Int?,
    val lastError: String?,
) {
    val canRetry: Boolean
        get() = status == UploadStatus.Failed
}
