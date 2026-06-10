package com.example.assistenttreneren.feature.upload.data.mapper

import com.example.assistenttreneren.core.database.entity.UploadJobEntity
import com.example.assistenttreneren.feature.upload.domain.model.UploadJob
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

fun UploadJobEntity.toUploadJob(): UploadJob =
    UploadJob(
        uploadJobId = uploadJobId,
        recordingId = recordingId,
        activityId = activityId,
        backendUploadId = backendUploadId,
        status = status.toUploadStatus(),
        statusMessage = statusMessage,
        progressPercent = progressPercent,
        attemptCount = attemptCount,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        lastError = lastError,
    )

fun UploadJob.toUploadJobEntity(): UploadJobEntity =
    UploadJobEntity(
        uploadJobId = uploadJobId,
        recordingId = recordingId,
        activityId = activityId,
        backendUploadId = backendUploadId,
        status = status.name,
        statusMessage = statusMessage,
        progressPercent = progressPercent,
        attemptCount = attemptCount,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        lastError = lastError,
    )

private fun String.toUploadStatus(): UploadStatus =
    runCatching { UploadStatus.valueOf(this) }
        .getOrDefault(UploadStatus.Failed)
