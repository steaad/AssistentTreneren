package com.example.assistenttreneren.feature.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangeInitialPasswordRequestDto(
    val email: String,
    val temporaryPassword: String,
    val newPassword: String,
)
