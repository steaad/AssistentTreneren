package com.example.assistenttreneren.feature.recording.domain.model

data class RecordingSession(
    val recordingId: String,
    val activityId: String?,
    val displayName: String,
    val contentUri: String,
    val mediaType: RecordingMediaType,
    val mimeType: String,
    val durationMillis: Long,
    val category: String,
    val subCategory: String,
    val createdAtMillis: Long,
    val uploadStatus: RecordingUploadStatus = RecordingUploadStatus.AvailableForUpload,
)
