package com.example.assistenttreneren.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analysis_metadata",
    indices = [
        Index(value = ["activityId"]),
        Index(value = ["createdAtMillis"]),
    ],
)
data class AnalysisMetadataEntity(
    @PrimaryKey
    val analysisId: String,
    val activityId: String?,
    val title: String?,
    val activityCategory: String?,
    val status: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
