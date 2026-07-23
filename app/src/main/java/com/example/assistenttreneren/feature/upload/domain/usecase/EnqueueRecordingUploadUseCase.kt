package com.example.assistenttreneren.feature.upload.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.model.RecordingUploadStatus
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.upload.data.worker.UploadRecordingWorker
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class EnqueueRecordingUploadUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localRecordingRepository: LocalRecordingRepository,
    private val localUploadRepository: LocalUploadRepository,
) {
    suspend operator fun invoke(recording: RecordingSession): UploadJob {
        val existingJob = localUploadRepository.getLatestUploadJobForRecording(recording.recordingId)
        if (existingJob != null && existingJob.status.isActive) {
            return existingJob
        }

        if (existingJob?.status == UploadStatus.Completed) {
            localRecordingRepository.updateUploadStatus(
                recordingId = recording.recordingId,
                uploadStatus = RecordingUploadStatus.Uploaded,
            )
            return existingJob
        }

        localRecordingRepository.updateUploadStatus(
            recordingId = recording.recordingId,
            uploadStatus = RecordingUploadStatus.UploadInProgress,
        )

        val now = System.currentTimeMillis()
        val uploadJob = UploadJob(
            uploadJobId = UUID.randomUUID().toString(),
            recordingId = recording.recordingId,
            activityId = recording.activityId,
            backendUploadId = null,
            status = UploadStatus.Queued,
            statusMessage = "Opptaket er satt i kø.",
            progressPercent = 0,
            attemptCount = 0,
            createdAtMillis = now,
            updatedAtMillis = now,
            lastError = null,
        )

        localUploadRepository.saveUploadJob(uploadJob)
        enqueue(uploadJob.uploadJobId)
        return uploadJob
    }

    fun retry(uploadJob: UploadJob) {
        enqueue(uploadJob.uploadJobId)
    }

    private fun enqueue(uploadJobId: String) {
        val request = OneTimeWorkRequestBuilder<UploadRecordingWorker>()
            .setInputData(workDataOf(UploadRecordingWorker.KEY_UPLOAD_JOB_ID to uploadJobId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueUploadWorkName(uploadJobId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private val UploadStatus.isActive: Boolean
        get() = when (this) {
            UploadStatus.Queued,
            UploadStatus.Uploading,
            UploadStatus.ProcessingAudio,
            UploadStatus.Transcribing,
            -> true

            UploadStatus.Completed,
            UploadStatus.Failed,
            -> false
        }

    private fun uniqueUploadWorkName(uploadJobId: String): String =
        "upload-recording-$uploadJobId"
}
