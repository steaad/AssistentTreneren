package com.example.assistenttreneren.feature.upload.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.assistenttreneren.feature.upload.data.dto.UploadRecordingResponseDto
import com.example.assistenttreneren.feature.upload.data.dto.UploadStatusResponseDto
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UploadApi {
    @Multipart
    @POST("api/activities/{activityId}/recordings")
    suspend fun uploadRecording(
        @Path("activityId") activityId: String,
        @Part media: MultipartBody.Part,
        @Part("metadata") metadata: RequestBody,
    ): UploadRecordingResponseDto

    @GET("api/uploads/{uploadId}/status")
    suspend fun getUploadStatus(
        @Path("uploadId") uploadId: String,
    ): UploadStatusResponseDto
}
