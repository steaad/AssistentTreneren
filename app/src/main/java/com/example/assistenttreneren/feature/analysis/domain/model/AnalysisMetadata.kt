package com.example.assistenttreneren.feature.analysis.domain.model

data class AnalysisMetadata(
    val analysisId: String,
    val activityId: String?,
    val title: String?,
    val activityCategory: String?,
    val status: AnalysisStatus,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
