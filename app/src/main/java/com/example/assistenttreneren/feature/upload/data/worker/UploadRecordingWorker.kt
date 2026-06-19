package com.example.assistenttreneren.feature.upload.data.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

        val startedJob = uploadJob.copy(
            status = UploadStatus.Uploading,
            statusMessage = "Laster opp opptak.",
            progressPercent = 0,
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
            val metadataBody = json.encodeToString(metadata)
                .toRequestBody(JSON_MEDIA_TYPE)
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

            val uploadResponse = uploadApi.uploadRecording(
                activityId = activityId,
                media = mediaPart,
                metadata = metadataBody,
            )
            val responseJob = startedJob.copy(
                backendUploadId = uploadResponse.uploadId,
                status = uploadResponse.status.toUploadStatusFromDto(),
                statusMessage = uploadResponse.statusMessage,
                progressPercent = if (uploadResponse.status == UploadStatus.Completed.name) {
                    100
                } else {
                    startedJob.progressPercent
                },
                updatedAtMillis = System.currentTimeMillis(),
                lastError = null,
            )
            localUploadRepository.saveUploadJob(responseJob)
            pollUploadStatus(responseJob)
            Result.success()
        } catch (exception: IOException) {
            failUpload(startedJob, "Nettverksfeil under opplasting.")
        } catch (exception: HttpException) {
            failUpload(startedJob, "Serverfeil under opplasting (${exception.code()}).")
        } catch (exception: Exception) {
            failUpload(startedJob, exception.message ?: "Kunne ikke laste opp opptaket.")
        }
    }

    private suspend fun pollUploadStatus(uploadJob: UploadJob) {
        val backendUploadId = uploadJob.backendUploadId ?: return
        var currentJob = uploadJob

        repeat(MAX_STATUS_POLLS) {
            if (currentJob.status.isTerminal) {
                return
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
        }
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
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private val json = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }
}
