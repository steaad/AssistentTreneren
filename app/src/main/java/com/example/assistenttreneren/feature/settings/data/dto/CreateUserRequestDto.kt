package com.example.assistenttreneren.feature.settings.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequestDto(
    val email: String,
    val displayName: String,
    val role: String,
    val temporaryPassword: String,
)
