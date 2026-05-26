package com.example.assistenttreneren.feature.login.data.remote

import com.example.assistenttreneren.feature.login.data.dto.LoginRequestDto
import com.example.assistenttreneren.feature.login.data.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): LoginResponseDto
}
