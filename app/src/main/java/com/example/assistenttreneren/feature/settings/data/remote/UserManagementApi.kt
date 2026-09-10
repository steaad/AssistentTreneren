package com.example.assistenttreneren.feature.settings.data.remote

import com.example.assistenttreneren.feature.settings.data.dto.CreateUserRequestDto
import com.example.assistenttreneren.feature.settings.data.dto.CreatedUserResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface UserManagementApi {
    @POST("api/users")
    suspend fun createUser(@Body request: CreateUserRequestDto): CreatedUserResponseDto
}
