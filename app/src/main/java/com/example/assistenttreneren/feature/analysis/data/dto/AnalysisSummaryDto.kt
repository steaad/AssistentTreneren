package com.example.assistenttreneren.feature.analysis.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisSummaryDto(
    val analysisId: String,
    val activityId: String? = null,
    val title: String? = null,
    val activityCategory: String? = null,
    val status: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
