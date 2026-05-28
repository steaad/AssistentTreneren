package com.example.assistenttreneren.feature.activitywizard.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoachActivityDto(
    val activityId: String,
    val activityCategory: String? = null,
    val title: String? = null,
    val recordings: List<RecordingDto> = emptyList(),
)
