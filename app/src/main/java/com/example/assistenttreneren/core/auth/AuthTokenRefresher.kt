package com.example.assistenttreneren.core.auth

interface AuthTokenRefresher {
    suspend fun refreshTokens(): AuthTokens?
}
