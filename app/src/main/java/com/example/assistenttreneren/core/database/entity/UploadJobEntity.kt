package com.example.assistenttreneren.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "upload_jobs",
    indices = [
        Index(value = ["recordingId"]),
        Index(value = ["activityId"]),
        Index(value = ["backendUploadId"]),
        Index(value = ["status"]),
    ],
)
data class UploadJobEntity(
    @PrimaryKey
    val uploadJobId: String,
    val recordingId: String,
    val activityId: String?,
    val backendUploadId: String?,
    val status: String,
    val statusMessage: String?,
    val progressPercent: Int?,
    val attemptCount: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val lastError: String?,
)
