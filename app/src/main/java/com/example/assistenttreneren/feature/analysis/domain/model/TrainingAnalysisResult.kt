package com.example.assistenttreneren.feature.analysis.domain.model

data class TrainingAnalysisResult(
    val executiveSummary: ExecutiveSummary,
    val learningSummary: LearningSummary,
    val teamDevelopmentSummary: EvidenceGroup,
    val playerDevelopmentSummary: EvidenceGroup,
    val patterns: List<AnalysisEvidence>,
    val priorities: List<AnalysisEvidence>,
    val recommendations: List<AnalysisEvidence>,
    val uncertainties: List<AnalysisEvidence>,
    val coachInterventions: List<CoachIntervention>,
    val training: TrainingAnalysis,
)

data class LearningSummary(
    val statedLearningObjectives: List<String>,
    val whatPlayersAppearToUnderstand: List<String>,
    val whatPlayersStillStruggleWith: List<String>,
    val nextLearningPriorities: List<String>,
)

data class EvidenceGroup(
    val overallSummary: String,
    val players: List<AnalysisEvidence>,
)

data class TrainingAnalysis(
    val summary: String,
    val exercise: TrainingSection?,
    val game: TrainingSection?,
    val evaluation: TrainingSection?,
    val learningProgressions: List<AnalysisEvidence>,
    val playerFollowUp: List<AnalysisEvidence>,
)

data class TrainingSection(
    val summary: String,
    val findings: List<AnalysisEvidence>,
    val uncertainties: List<AnalysisEvidence>,
)
