package com.example.assistenttreneren.feature.analysis.domain.model

import kotlinx.serialization.json.JsonElement

enum class AnalysisCandidateState {
    READY,
    NO_AUDIO_RECORDINGS,
    TRANSCRIPTION_PENDING,
    REVIEW_REQUIRED,
    INPUT_INVALID,
    PROCESSING,
    COMPLETED,
}
enum class AnalysisJobStatus { QUEUED, PROCESSING, COMPLETED, FAILED }

data class AnalysisCandidate(
    val activityId: String,
    val title: String?,
    val state: AnalysisCandidateState,
    val message: String,
    val latestAnalysis: AnalysisJob?,
)

data class AnalysisJob(
    val analysisId: String,
    val activityId: String,
    val status: AnalysisJobStatus,
    val schemaVersion: String,
    val promptVersion: String,
    val model: String,
    val createdAt: String,
    val completedAt: String?,
    val errorMessage: String?,
    val result: JsonElement?,
    val activityCategory: String? = null,
    val startedAt: String? = null,
    val processingDurationMillis: Long? = null,
    val dataBasis: AnalysisDataBasis? = null,
)

data class AnalysisDataBasis(
    val recordingCount: Int,
    val eventCount: Int,
)
