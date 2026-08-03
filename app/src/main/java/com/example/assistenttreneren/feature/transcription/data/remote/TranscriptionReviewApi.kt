package com.example.assistenttreneren.feature.transcription.data.remote

import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventRequestDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionReviewDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TranscriptionReviewApi {
    @GET("api/activities/{activityId}/transcription-review")
    suspend fun getReview(@Path("activityId") activityId: String): TranscriptionReviewDto

    @POST("api/transcription-event-issues/{issueId}/resolve")
    suspend fun resolveIssue(@Path("issueId") issueId: String, @Body request: TranscriptionEventRequestDto)

    @POST("api/transcription-event-issues/{issueId}/dismiss")
    suspend fun dismissIssue(@Path("issueId") issueId: String)

    @PATCH("api/transcription-events/{eventId}")
    suspend fun updateEvent(@Path("eventId") eventId: String, @Body request: TranscriptionEventRequestDto)

    @DELETE("api/transcription-events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: String)
}
