package com.example.assistenttreneren.core.auth

interface TokenStorage {
    suspend fun saveTokens(tokens: AuthTokens)

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun clearTokens()
}
