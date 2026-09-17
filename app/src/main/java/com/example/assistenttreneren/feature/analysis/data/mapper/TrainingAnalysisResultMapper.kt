package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisEvidenceDto
import com.example.assistenttreneren.feature.analysis.data.dto.EvidenceGroupDto
import com.example.assistenttreneren.feature.analysis.data.dto.CoachInterventionDto
import com.example.assistenttreneren.feature.analysis.data.dto.LearningSummaryDto
import com.example.assistenttreneren.feature.analysis.data.dto.TrainingAnalysisDto
import com.example.assistenttreneren.feature.analysis.data.dto.TrainingAnalysisResultDto
import com.example.assistenttreneren.feature.analysis.data.dto.TrainingSectionDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisEvidence
import com.example.assistenttreneren.feature.analysis.domain.model.EvidenceGroup
import com.example.assistenttreneren.feature.analysis.domain.model.ExecutiveSummary
import com.example.assistenttreneren.feature.analysis.domain.model.LearningSummary
import com.example.assistenttreneren.feature.analysis.domain.model.TrainingAnalysis
import com.example.assistenttreneren.feature.analysis.domain.model.TrainingAnalysisResult
import com.example.assistenttreneren.feature.analysis.domain.model.TrainingSection
import com.example.assistenttreneren.feature.analysis.domain.model.CoachIntervention

fun TrainingAnalysisResultDto.toTrainingAnalysisResult() = TrainingAnalysisResult(
    executiveSummary = ExecutiveSummary(
        title = executiveSummary.title,
        summary = executiveSummary.summary,
        keyTakeaways = executiveSummary.keyTakeaways,
        positiveDevelopments = executiveSummary.positiveDevelopments,
        mainConcerns = executiveSummary.mainConcerns,
    ),
    learningSummary = learningSummary.toLearningSummary(),
    teamDevelopmentSummary = teamDevelopmentSummary.toEvidenceGroup(),
    playerDevelopmentSummary = playerDevelopmentSummary.toEvidenceGroup(),
    patterns = patterns.map { it.toTrainingEvidence() },
    priorities = priorities.map { it.toTrainingEvidence() },
    recommendations = recommendations.map { it.toTrainingEvidence() },
    uncertainties = uncertainties.map { it.toTrainingEvidence() },
    coachInterventions = coachInterventions.map { it.toCoachIntervention() },
    training = categoryAnalysis.training.toTrainingAnalysis(),
)

private fun CoachInterventionDto.toCoachIntervention() = CoachIntervention(
    type, summary, section, confidence, relatedEventIds, relatedTranscriptionIds,
)

private fun LearningSummaryDto.toLearningSummary() = LearningSummary(
    statedLearningObjectives, whatPlayersAppearToUnderstand, whatPlayersStillStruggleWith, nextLearningPriorities,
)

private fun EvidenceGroupDto.toEvidenceGroup() = EvidenceGroup(overallSummary, players.map { it.toTrainingEvidence() })

private fun TrainingAnalysisDto.toTrainingAnalysis() = TrainingAnalysis(
    summary = summary,
    exercise = exercise?.toTrainingSection(),
    game = game?.toTrainingSection(),
    evaluation = evaluation?.toTrainingSection(),
    learningProgressions = learningProgressions.map { it.toTrainingEvidence() },
    playerFollowUp = playerFollowUp.map { it.toTrainingEvidence() },
)

private fun TrainingSectionDto.toTrainingSection() = TrainingSection(
    summary, findings.map { it.toTrainingEvidence() }, uncertainties.map { it.toTrainingEvidence() },
)

private fun AnalysisEvidenceDto.toTrainingEvidence() = AnalysisEvidence(
    title, description, relatedEventIds, relatedTranscriptionIds, relatedPlayerNames,
)
