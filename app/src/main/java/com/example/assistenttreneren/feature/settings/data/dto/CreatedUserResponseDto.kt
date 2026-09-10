package com.example.assistenttreneren.feature.settings.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatedUserResponseDto(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String,
)
