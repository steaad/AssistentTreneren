package com.example.assistenttreneren.feature.upload.data.remote

import okhttp3.MultipartBody
import com.example.assistenttreneren.feature.upload.data.dto.UploadRecordingMetadataDto
import com.example.assistenttreneren.feature.upload.data.dto.UploadRecordingResponseDto
import com.example.assistenttreneren.feature.upload.data.dto.UploadStatusResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UploadApi {
    @POST("api/activities/{activityId}/uploads")
    suspend fun createUpload(
        @Path("activityId") activityId: String,
        @Body metadata: UploadRecordingMetadataDto,
    ): UploadRecordingResponseDto

    @Multipart
    @POST("api/uploads/{uploadId}/media")
    suspend fun uploadMedia(
        @Path("uploadId") uploadId: String,
        @Part media: MultipartBody.Part,
    )

    @GET("api/uploads/{uploadId}/status")
    suspend fun getUploadStatus(
        @Path("uploadId") uploadId: String,
    ): UploadStatusResponseDto
}
