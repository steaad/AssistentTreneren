package com.example.assistenttreneren.feature.activitywizard.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCoachActivityRequestDto(
    val activityCategory: String? = null,
    val title: String? = null,
)
