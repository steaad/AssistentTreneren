package com.example.assistenttreneren.feature.activitywizard.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecordingDto(
    val id: String,
    val recordingType: String,
    val filename: String,
    val duration: Long,
)
