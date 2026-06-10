package com.example.assistenttreneren.feature.analysis.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AnalysisDetailDto(
    val analysisId: String,
    val activityId: String? = null,
    val title: String? = null,
    val activityCategory: String? = null,
    val status: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val content: JsonElement? = null,
)
