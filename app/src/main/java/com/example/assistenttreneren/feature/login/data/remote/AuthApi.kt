package com.example.assistenttreneren.feature.login.data.remote

import com.example.assistenttreneren.core.network.AuthInterceptor
import com.example.assistenttreneren.feature.login.data.dto.LoginRequestDto
import com.example.assistenttreneren.feature.login.data.dto.LoginResponseDto
import com.example.assistenttreneren.feature.login.data.dto.ChangeInitialPasswordRequestDto
import com.example.assistenttreneren.feature.login.data.dto.RefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("${AuthInterceptor.NO_AUTH_HEADER}: true")
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): LoginResponseDto

    @Headers("${AuthInterceptor.NO_AUTH_HEADER}: true")
    @POST("api/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequestDto,
    ): LoginResponseDto

    @Headers("${AuthInterceptor.NO_AUTH_HEADER}: true")
    @POST("api/auth/change-initial-password")
    suspend fun changeInitialPassword(
        @Body request: ChangeInitialPasswordRequestDto,
    ): LoginResponseDto
}
