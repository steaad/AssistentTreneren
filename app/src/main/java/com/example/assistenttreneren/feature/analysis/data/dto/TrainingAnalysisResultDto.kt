package com.example.assistenttreneren.feature.analysis.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainingAnalysisResultDto(
    val executiveSummary: ExecutiveSummaryDto,
    val learningSummary: LearningSummaryDto,
    val teamDevelopmentSummary: EvidenceGroupDto,
    val playerDevelopmentSummary: EvidenceGroupDto,
    val patterns: List<AnalysisEvidenceDto>,
    val priorities: List<AnalysisEvidenceDto>,
    val recommendations: List<AnalysisEvidenceDto>,
    val uncertainties: List<AnalysisEvidenceDto>,
    val coachInterventions: List<CoachInterventionDto> = emptyList(),
    val categoryAnalysis: TrainingCategoryAnalysisDto,
)

@Serializable
data class TrainingCategoryAnalysisDto(
    val training: TrainingAnalysisDto,
)

@Serializable
data class TrainingAnalysisDto(
    val summary: String,
    val exercise: TrainingSectionDto? = null,
    val game: TrainingSectionDto? = null,
    val evaluation: TrainingSectionDto? = null,
    val learningProgressions: List<AnalysisEvidenceDto>,
    val playerFollowUp: List<AnalysisEvidenceDto>,
)

@Serializable
data class TrainingSectionDto(
    val summary: String,
    val findings: List<AnalysisEvidenceDto>,
    val uncertainties: List<AnalysisEvidenceDto>,
)

@Serializable
data class LearningSummaryDto(
    val statedLearningObjectives: List<String>,
    val whatPlayersAppearToUnderstand: List<String>,
    val whatPlayersStillStruggleWith: List<String>,
    val nextLearningPriorities: List<String>,
)

@Serializable
data class EvidenceGroupDto(
    val overallSummary: String,
    val players: List<AnalysisEvidenceDto>,
)
