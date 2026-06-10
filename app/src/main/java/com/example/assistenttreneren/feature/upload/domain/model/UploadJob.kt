package com.example.assistenttreneren.feature.upload.domain.model

data class UploadJob(
    val uploadJobId: String,
    val recordingId: String,
    val activityId: String?,
    val backendUploadId: String?,
    val status: UploadStatus,
    val statusMessage: String?,
    val progressPercent: Int?,
    val attemptCount: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val lastError: String?,
)
