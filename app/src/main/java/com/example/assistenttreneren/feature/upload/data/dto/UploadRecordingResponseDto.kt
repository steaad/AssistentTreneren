package com.example.assistenttreneren.feature.upload.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadRecordingResponseDto(
    val uploadId: String,
    val recordingId: String,
    val status: String,
    val statusMessage: String? = null,
)
