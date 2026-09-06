package com.example.assistenttreneren.feature.analysis.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AnalysisCandidateDto(
    val activityId: String,
    val title: String? = null,
    val state: String,
    val message: String,
    val latestAnalysis: AnalysisJobDto? = null,
)

@Serializable
data class AnalysisJobDto(
    val analysisId: String,
    val activityId: String,
    val status: String,
    val schemaVersion: String,
    val promptVersion: String,
    val model: String,
    val createdAt: String,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val errorMessage: String? = null,
    val result: JsonElement? = null,
)
