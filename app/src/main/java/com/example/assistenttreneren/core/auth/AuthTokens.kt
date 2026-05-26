package com.example.assistenttreneren.core.auth

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)
