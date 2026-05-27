package com.example.assistenttreneren.feature.login.data.mapper

import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.feature.login.data.dto.LoginResponseDto

fun LoginResponseDto.toAuthTokens(): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)
