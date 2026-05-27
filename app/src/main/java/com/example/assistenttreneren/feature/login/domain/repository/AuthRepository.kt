package com.example.assistenttreneren.feature.login.domain.repository

import com.example.assistenttreneren.core.auth.AuthTokens

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): AuthResult<AuthTokens>
}
