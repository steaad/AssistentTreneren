package com.example.assistenttreneren.feature.upload.data.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.model.RecordingUploadStatus
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.upload.data.dto.UploadRecordingMetadataDto
import com.example.assistenttreneren.feature.upload.data.mapper.toUploadJob
import com.example.assistenttreneren.feature.upload.data.mapper.toUploadStatusFromDto
import com.example.assistenttreneren.feature.upload.data.remote.UploadApi
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import retrofit2.HttpException

@HiltWorker
class UploadRecordingWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val uploadApi: UploadApi,
    private val localRecordingRepository: LocalRecordingRepository,
    private val localUploadRepository: LocalUploadRepository,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val uploadJobId = inputData.getString(KEY_UPLOAD_JOB_ID)
            ?: return Result.failure()
        val uploadJob = localUploadRepository.getUploadJob(uploadJobId)
            ?: return Result.failure()
        val recording = localRecordingRepository.getRecording(uploadJob.recordingId)
            ?: return failUpload(uploadJob, "Opptaket finnes ikke lokalt.")
        val activityId = recording.activityId
            ?: return failUpload(uploadJob, "Opptaket mangler aktivitet.")

        val shouldUploadMedia = uploadJob.requiresMediaUpload()
        val startedJob = uploadJob.copy(
            status = if (shouldUploadMedia) UploadStatus.Uploading else uploadJob.status,
            statusMessage = if (shouldUploadMedia) "Laster opp opptak." else uploadJob.statusMessage,
            progressPercent = if (shouldUploadMedia) 0 else uploadJob.progressPercent,
            attemptCount = uploadJob.attemptCount + 1,
            updatedAtMillis = System.currentTimeMillis(),
            lastError = null,
        )
        localUploadRepository.saveUploadJob(startedJob)

        return try {
            val metadata = UploadRecordingMetadataDto(
                recordingId = recording.recordingId,
                filename = recording.displayName,
                durationMillis = recording.durationMillis,
                category = recording.category,
                subCategory = recording.subCategory,
                createdAtMillis = recording.createdAtMillis,
                mediaType = recording.mediaType.name,
                mimeType = recording.mimeType,
            )
            val uploadWithBackendId = createUploadIfNeeded(
                uploadJob = startedJob,
                activityId = activityId,
                metadata = metadata,
            )
            if (shouldUploadMedia) {
                uploadMedia(uploadWithBackendId, recording)
            }

            val uploadToPoll = if (shouldUploadMedia) {
                val mediaUploadedJob = uploadWithBackendId.copy(
                    status = UploadStatus.Queued,
                    statusMessage = "Media lastet opp. Venter på behandling.",
                    progressPercent = 0,
                    updatedAtMillis = System.currentTimeMillis(),
                    lastError = null,
                )
                localUploadRepository.saveUploadJob(mediaUploadedJob)
                mediaUploadedJob
            } else {
                uploadWithBackendId
            }

            if (pollUploadStatus(uploadToPoll)) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (exception: IOException) {
            failUpload(startedJob, "Nettverksfeil under opplasting.")
        } catch (exception: HttpException) {
            failUpload(startedJob, "Serverfeil under opplasting (${exception.code()}).")
        } catch (exception: Exception) {
            failUpload(startedJob, exception.message ?: "Kunne ikke laste opp opptaket.")
        }
    }

    private suspend fun uploadMedia(
        uploadJob: UploadJob,
        recording: RecordingSession,
    ) {
        val mediaBody = ContentUriRequestBody(
            contentResolver = appContext.contentResolver,
            uri = Uri.parse(recording.contentUri),
            mediaType = recording.mimeType.toMediaType(),
        )
        val mediaPart = MultipartBody.Part.createFormData(
            name = MEDIA_PART_NAME,
            filename = recording.displayName,
            body = mediaBody,
        )
        uploadApi.uploadMedia(
            uploadId = uploadJob.backendUploadId
                ?: throw IllegalStateException("Uploaden mangler backend-ID."),
            media = mediaPart,
        )
    }

    private fun UploadJob.requiresMediaUpload(): Boolean =
        backendUploadId.isNullOrBlank() || status == UploadStatus.Uploading

    private suspend fun createUploadIfNeeded(
        uploadJob: UploadJob,
        activityId: String,
        metadata: UploadRecordingMetadataDto,
    ): UploadJob {
        if (!uploadJob.backendUploadId.isNullOrBlank()) {
            return uploadJob
        }

        val response = uploadApi.createUpload(
            activityId = activityId,
            metadata = metadata,
        )
        val backendUploadId = response.uploadId.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Backend returnerte en upload uten ID.")
        val createdUploadJob = uploadJob.copy(
            backendUploadId = backendUploadId,
            status = response.status.toUploadStatusFromDto(),
            statusMessage = response.statusMessage,
            progressPercent = 0,
            updatedAtMillis = System.currentTimeMillis(),
            lastError = null,
        )
        localUploadRepository.saveUploadJob(createdUploadJob)
        return createdUploadJob
    }

    private suspend fun pollUploadStatus(uploadJob: UploadJob): Boolean {
        val backendUploadId = uploadJob.backendUploadId ?: return false
        var currentJob = uploadJob

        repeat(MAX_STATUS_POLLS) {
            if (currentJob.status.isTerminal) {
                return true
            }

            delay(STATUS_POLL_DELAY_MILLIS)
            val statusResponse = uploadApi.getUploadStatus(backendUploadId)
            currentJob = statusResponse.toUploadJob(
                uploadJobId = currentJob.uploadJobId,
                attemptCount = currentJob.attemptCount,
                createdAtMillis = currentJob.createdAtMillis,
                lastError = null,
            )
            localUploadRepository.saveUploadJob(currentJob)
            if (currentJob.status == UploadStatus.Completed) {
                localRecordingRepository.updateUploadStatus(
                    recordingId = currentJob.recordingId,
                    uploadStatus = RecordingUploadStatus.Uploaded,
                )
            }
        }

        return currentJob.status.isTerminal
    }

    private suspend fun failUpload(
        uploadJob: UploadJob,
        message: String,
    ): Result {
        localUploadRepository.saveUploadJob(
            uploadJob.copy(
                status = UploadStatus.Failed,
                statusMessage = message,
                updatedAtMillis = System.currentTimeMillis(),
                lastError = message,
            ),
        )
        return Result.failure()
    }

    private val UploadStatus.isTerminal: Boolean
        get() = this == UploadStatus.Completed || this == UploadStatus.Failed

    companion object {
        const val KEY_UPLOAD_JOB_ID = "upload_job_id"

        private const val MEDIA_PART_NAME = "media"
        private const val MAX_STATUS_POLLS = 12
        private const val STATUS_POLL_DELAY_MILLIS = 2_500L
    }
}
