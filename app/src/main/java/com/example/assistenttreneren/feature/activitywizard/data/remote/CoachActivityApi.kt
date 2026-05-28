package com.example.assistenttreneren.feature.activitywizard.data.remote

import com.example.assistenttreneren.feature.activitywizard.data.dto.CoachActivityDto
import com.example.assistenttreneren.feature.activitywizard.data.dto.CreateCoachActivityRequestDto
import com.example.assistenttreneren.feature.activitywizard.data.dto.UpdateCoachActivityRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CoachActivityApi {
    @POST("api/activities")
    suspend fun createActivity(
        @Body request: CreateCoachActivityRequestDto = CreateCoachActivityRequestDto,
    ): CoachActivityDto

    @GET("api/activities")
    suspend fun getActivities(): List<CoachActivityDto>

    @PATCH("api/activities/{activityId}")
    suspend fun updateActivity(
        @Path("activityId") activityId: String,
        @Body request: UpdateCoachActivityRequestDto,
    ): CoachActivityDto

    @GET("api/activities/{activityId}")
    suspend fun getActivity(
        @Path("activityId") activityId: String,
    ): CoachActivityDto
}
