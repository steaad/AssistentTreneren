package com.example.assistenttreneren.feature.upload.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadStatusResponseDto(
    val uploadId: String,
    val recordingId: String,
    val activityId: String? = null,
    val status: String,
    val statusMessage: String? = null,
    val progressPercent: Int? = null,
    val analysisId: String? = null,
)
