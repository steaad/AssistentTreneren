package com.example.assistenttreneren.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_recordings",
    indices = [
        Index(value = ["activityId"]),
        Index(value = ["createdAtMillis"]),
    ],
)
data class LocalRecordingEntity(
    @PrimaryKey
    val recordingId: String,
    val activityId: String?,
    val displayName: String,
    val contentUri: String,
    val durationMillis: Long,
    val category: String,
    val subCategory: String,
    val createdAtMillis: Long,
)
