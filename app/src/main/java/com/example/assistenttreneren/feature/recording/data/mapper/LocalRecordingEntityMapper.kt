package com.example.assistenttreneren.feature.recording.data.mapper

import com.example.assistenttreneren.core.database.entity.LocalRecordingEntity
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.model.RecordingUploadStatus

fun LocalRecordingEntity.toRecordingSession(): RecordingSession =
    RecordingSession(
        recordingId = recordingId,
        activityId = activityId,
        displayName = displayName,
        contentUri = contentUri,
        mediaType = mediaType.toRecordingMediaType(),
        mimeType = mimeType,
        durationMillis = durationMillis,
        category = category,
        subCategory = subCategory,
        createdAtMillis = createdAtMillis,
        matchPeriod = matchPeriod,
        matchClockStartMillis = matchClockStartMillis,
        uploadStatus = uploadStatus.toRecordingUploadStatus(),
    )

fun RecordingSession.toLocalRecordingEntity(): LocalRecordingEntity =
    LocalRecordingEntity(
        recordingId = recordingId,
        activityId = activityId,
        displayName = displayName,
        contentUri = contentUri,
        mediaType = mediaType.name,
        mimeType = mimeType,
        durationMillis = durationMillis,
        category = category,
        subCategory = subCategory,
        createdAtMillis = createdAtMillis,
        matchPeriod = matchPeriod,
        matchClockStartMillis = matchClockStartMillis,
        uploadStatus = uploadStatus.name,
    )

private fun String.toRecordingMediaType(): RecordingMediaType =
    runCatching { RecordingMediaType.valueOf(this) }
        .getOrDefault(RecordingMediaType.Audio)

private fun String.toRecordingUploadStatus(): RecordingUploadStatus =
    runCatching { RecordingUploadStatus.valueOf(this) }
        .getOrDefault(RecordingUploadStatus.AvailableForUpload)
