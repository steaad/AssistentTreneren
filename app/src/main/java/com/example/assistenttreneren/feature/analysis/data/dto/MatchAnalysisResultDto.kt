package com.example.assistenttreneren.feature.analysis.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchAnalysisResultDto(
    val executiveSummary: ExecutiveSummaryDto,
    val playerDevelopmentSummary: PlayerDevelopmentSummaryDto,
    val patterns: List<AnalysisEvidenceDto>,
    val priorities: List<AnalysisEvidenceDto>,
    val recommendations: List<AnalysisEvidenceDto>,
    val uncertainties: List<AnalysisEvidenceDto>,
    val categoryAnalysis: MatchCategoryAnalysisDto,
)

@Serializable
data class ExecutiveSummaryDto(
    val title: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val positiveDevelopments: List<String>,
    val mainConcerns: List<String>,
)

@Serializable
data class PlayerDevelopmentSummaryDto(
    val overallSummary: String,
    val teamDevelopmentPriorities: List<AnalysisEvidenceDto>,
    val players: List<AnalysisEvidenceDto>,
)

@Serializable
data class AnalysisEvidenceDto(
    val title: String,
    val description: String,
    val relatedEventIds: List<String>,
    val relatedTranscriptionIds: List<String>,
    val relatedPlayerNames: List<String>,
)

@Serializable
data class MatchCategoryAnalysisDto(
    val match: MatchCategoryDto,
)

@Serializable
data class MatchCategoryDto(
    val summary: MatchSummaryDto,
    val phaseComparisons: List<AnalysisEvidenceDto>,
    val playerFollowUp: List<AnalysisEvidenceDto>,
    val firstHalf: List<MatchPhaseDto>,
    val halftime: List<MatchPhaseDto>,
    val secondHalf: List<MatchPhaseDto>,
    val evaluation: List<MatchPhaseDto>,
)

@Serializable
data class MatchSummaryDto(
    val overallMatchAssessment: String,
    val matchStory: String,
    val mainStrengths: List<String>,
    val mainDevelopmentAreas: List<String>,
    val recommendedFollowUp: List<AnalysisEvidenceDto>,
)

@Serializable
data class MatchPhaseDto(
    val subcategory: String,
    val summary: String,
    val findings: List<AnalysisEvidenceDto>,
    val uncertainties: List<AnalysisEvidenceDto>,
)
