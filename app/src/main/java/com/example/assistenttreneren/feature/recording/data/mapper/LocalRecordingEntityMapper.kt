package com.example.assistenttreneren.feature.recording.data.mapper

import com.example.assistenttreneren.core.database.entity.LocalRecordingEntity
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession

fun LocalRecordingEntity.toRecordingSession(): RecordingSession =
    RecordingSession(
        recordingId = recordingId,
        activityId = activityId,
        displayName = displayName,
        contentUri = contentUri,
        durationMillis = durationMillis,
        category = category,
        subCategory = subCategory,
        createdAtMillis = createdAtMillis,
    )

fun RecordingSession.toLocalRecordingEntity(): LocalRecordingEntity =
    LocalRecordingEntity(
        recordingId = recordingId,
        activityId = activityId,
        displayName = displayName,
        contentUri = contentUri,
        durationMillis = durationMillis,
        category = category,
        subCategory = subCategory,
        createdAtMillis = createdAtMillis,
    )
