package com.example.assistenttreneren.feature.upload.domain.repository

import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import kotlinx.coroutines.flow.Flow

interface LocalUploadRepository {
    fun observeUploadJobsForActivity(activityId: String): Flow<List<UploadJob>>

    suspend fun getUploadJob(uploadJobId: String): UploadJob?

    suspend fun getLatestUploadJobForRecording(recordingId: String): UploadJob?

    suspend fun saveUploadJob(uploadJob: UploadJob)
}
