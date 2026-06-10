package com.example.assistenttreneren.feature.upload.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadRecordingMetadataDto(
    val recordingId: String,
    val filename: String,
    val durationMillis: Long,
    val category: String,
    val subCategory: String,
    val createdAtMillis: Long,
)
