package com.example.assistenttreneren.feature.upload.data.mapper

import com.example.assistenttreneren.feature.upload.data.dto.UploadStatusResponseDto
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

fun UploadStatusResponseDto.toUploadJob(
    uploadJobId: String,
    attemptCount: Int,
    createdAtMillis: Long,
    updatedAtMillis: Long = System.currentTimeMillis(),
    lastError: String? = null,
): UploadJob =
    UploadJob(
        uploadJobId = uploadJobId,
        recordingId = recordingId,
        activityId = activityId,
        backendUploadId = uploadId,
        status = status.toUploadStatusFromDto(),
        statusMessage = statusMessage,
        progressPercent = progressPercent,
        attemptCount = attemptCount,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        lastError = lastError,
    )

fun String.toUploadStatusFromDto(): UploadStatus =
    runCatching { UploadStatus.valueOf(this) }
        .getOrDefault(UploadStatus.Failed)
