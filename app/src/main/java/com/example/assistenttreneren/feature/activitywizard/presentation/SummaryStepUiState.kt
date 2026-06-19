package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

data class SummaryStepUiState(
    val activityId: String? = null,
    val recordings: List<SummaryRecordingUiModel> = emptyList(),
    val uploadJobs: List<SummaryUploadJobUiModel> = emptyList(),
) {
    val audioRecordingCount: Int
        get() = recordings.count { it.mediaType == RecordingMediaType.Audio }

    val videoRecordingCount: Int
        get() = recordings.count { it.mediaType == RecordingMediaType.Video }

    val completedUploadCount: Int
        get() = uploadJobs.count { it.status == UploadStatus.Completed }

    val failedUploadCount: Int
        get() = uploadJobs.count { it.status == UploadStatus.Failed }
}

data class SummaryRecordingUiModel(
    val recordingId: String,
    val displayName: String,
    val mediaType: RecordingMediaType,
    val durationMillis: Long,
    val subCategory: String,
)

data class SummaryUploadJobUiModel(
    val uploadJobId: String,
    val recordingDisplayName: String,
    val mediaType: RecordingMediaType,
    val status: UploadStatus,
    val statusMessage: String?,
    val progressPercent: Int?,
)
