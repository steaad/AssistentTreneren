package com.example.assistenttreneren.feature.activitywizard.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

interface LearningCatalogApi {
    @GET("api/learning-catalog/team-functions")
    suspend fun getTeamFunctions(): List<String>

    @GET("api/learning-catalog/themes")
    suspend fun getThemes(): List<LearningCatalogItemDto>

    @GET("api/learning-catalog/themes/{themeId}/subthemes")
    suspend fun getSubthemes(@Path("themeId") themeId: String): List<LearningCatalogItemDto>

    @GET("api/learning-catalog/themes/{themeId}/learning-objectives")
    suspend fun getLearningObjectives(@Path("themeId") themeId: String): List<LearningCatalogItemDto>

    @GET("api/activities/{activityId}/training-learning-config")
    suspend fun getTrainingLearningConfig(
        @Path("activityId") activityId: String,
    ): TrainingLearningConfigResponseDto

    @PUT("api/activities/{activityId}/training-learning-config")
    suspend fun updateTrainingLearningConfig(
        @Path("activityId") activityId: String,
        @Body request: TrainingLearningConfigRequestDto,
    )

    @POST("api/learning-catalog/themes")
    suspend fun createTheme(@Body request: UpsertLearningCatalogRequestDto)

    @POST("api/learning-catalog/themes/{themeId}/subthemes")
    suspend fun createSubtheme(@Path("themeId") themeId: String, @Body request: UpsertLearningCatalogRequestDto)

    @POST("api/learning-catalog/themes/{themeId}/learning-objectives")
    suspend fun createLearningObjective(@Path("themeId") themeId: String, @Body request: UpsertLearningCatalogRequestDto)

    @PATCH("api/learning-catalog/themes/{id}")
    suspend fun updateTheme(@Path("id") id: String, @Body request: UpsertLearningCatalogRequestDto)

    @PATCH("api/learning-catalog/subthemes/{id}")
    suspend fun updateSubtheme(@Path("id") id: String, @Body request: UpsertLearningCatalogRequestDto)

    @PATCH("api/learning-catalog/learning-objectives/{id}")
    suspend fun updateLearningObjective(@Path("id") id: String, @Body request: UpsertLearningCatalogRequestDto)

    @DELETE("api/learning-catalog/themes/{id}")
    suspend fun deleteTheme(@Path("id") id: String)

    @DELETE("api/learning-catalog/subthemes/{id}")
    suspend fun deleteSubtheme(@Path("id") id: String)

    @DELETE("api/learning-catalog/learning-objectives/{id}")
    suspend fun deleteLearningObjective(@Path("id") id: String)
}

@Serializable
data class LearningCatalogItemDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val parentId: String? = null,
    val teamFunction: String? = null,
)

@Serializable
data class TrainingLearningConfigRequestDto(
    val teamFunction: String,
    val themeId: String,
    val subthemeId: String? = null,
    val objectiveIds: List<String>,
)

@Serializable
data class TrainingLearningConfigResponseDto(
    val teamFunction: String? = null,
    val theme: LearningCatalogItemDto? = null,
    val subtheme: LearningCatalogItemDto? = null,
    val objectives: List<LearningCatalogItemDto> = emptyList(),
)

@Serializable
data class UpsertLearningCatalogRequestDto(
    val name: String,
    val description: String? = null,
    val teamFunction: String? = null,
    val subthemeId: String? = null,
)
