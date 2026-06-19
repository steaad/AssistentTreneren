package com.example.assistenttreneren.feature.upload.data.repository

import com.example.assistenttreneren.core.database.dao.UploadJobDao
import com.example.assistenttreneren.feature.upload.data.mapper.toUploadJob
import com.example.assistenttreneren.feature.upload.data.mapper.toUploadJobEntity
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalUploadRepositoryImpl @Inject constructor(
    private val uploadJobDao: UploadJobDao,
) : LocalUploadRepository {
    override fun observeUploadJobsForActivity(activityId: String): Flow<List<UploadJob>> =
        uploadJobDao.observeUploadJobsForActivity(activityId)
            .map { uploadJobs -> uploadJobs.map { it.toUploadJob() } }

    override suspend fun getUploadJob(uploadJobId: String): UploadJob? =
        uploadJobDao.getUploadJob(uploadJobId)?.toUploadJob()

    override suspend fun getLatestUploadJobForRecording(recordingId: String): UploadJob? =
        uploadJobDao.getLatestUploadJobForRecording(recordingId)?.toUploadJob()

    override suspend fun saveUploadJob(uploadJob: UploadJob) {
        uploadJobDao.upsertUploadJob(uploadJob.toUploadJobEntity())
    }
}
