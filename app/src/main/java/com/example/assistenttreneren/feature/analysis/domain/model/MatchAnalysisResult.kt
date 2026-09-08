package com.example.assistenttreneren.feature.analysis.domain.model

data class MatchAnalysisResult(
    val executiveSummary: ExecutiveSummary,
    val playerDevelopmentSummary: PlayerDevelopmentSummary,
    val patterns: List<AnalysisEvidence>,
    val priorities: List<AnalysisEvidence>,
    val recommendations: List<AnalysisEvidence>,
    val uncertainties: List<AnalysisEvidence>,
    val match: MatchCategoryAnalysis,
)

data class ExecutiveSummary(
    val title: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val positiveDevelopments: List<String>,
    val mainConcerns: List<String>,
)

data class PlayerDevelopmentSummary(
    val overallSummary: String,
    val teamDevelopmentPriorities: List<AnalysisEvidence>,
    val players: List<AnalysisEvidence>,
)

data class AnalysisEvidence(
    val title: String,
    val description: String,
    val relatedEventIds: List<String>,
    val relatedTranscriptionIds: List<String>,
    val relatedPlayerNames: List<String>,
)

data class MatchCategoryAnalysis(
    val summary: MatchSummary,
    val phaseComparisons: List<AnalysisEvidence>,
    val playerFollowUp: List<AnalysisEvidence>,
    val firstHalf: List<MatchPhase>,
    val halftime: List<MatchPhase>,
    val secondHalf: List<MatchPhase>,
    val evaluation: List<MatchPhase>,
)

data class MatchSummary(
    val overallMatchAssessment: String,
    val matchStory: String,
    val mainStrengths: List<String>,
    val mainDevelopmentAreas: List<String>,
    val recommendedFollowUp: List<AnalysisEvidence>,
)

data class MatchPhase(
    val subcategory: String,
    val summary: String,
    val findings: List<AnalysisEvidence>,
    val uncertainties: List<AnalysisEvidence>,
)
