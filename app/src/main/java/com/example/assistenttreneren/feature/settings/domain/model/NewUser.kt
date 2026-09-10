package com.example.assistenttreneren.feature.settings.domain.model

import com.example.assistenttreneren.core.auth.UserRole

data class NewUser(
    val email: String,
    val displayName: String,
    val role: UserRole,
    val temporaryPassword: String,
)
