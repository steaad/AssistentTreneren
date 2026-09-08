package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisEvidenceDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchAnalysisResultDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchCategoryDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchPhaseDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchSummaryDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisEvidence
import com.example.assistenttreneren.feature.analysis.domain.model.ExecutiveSummary
import com.example.assistenttreneren.feature.analysis.domain.model.MatchAnalysisResult
import com.example.assistenttreneren.feature.analysis.domain.model.MatchCategoryAnalysis
import com.example.assistenttreneren.feature.analysis.domain.model.MatchPhase
import com.example.assistenttreneren.feature.analysis.domain.model.MatchSummary
import com.example.assistenttreneren.feature.analysis.domain.model.PlayerDevelopmentSummary

fun MatchAnalysisResultDto.toMatchAnalysisResult() = MatchAnalysisResult(
    executiveSummary = ExecutiveSummary(
        title = executiveSummary.title,
        summary = executiveSummary.summary,
        keyTakeaways = executiveSummary.keyTakeaways,
        positiveDevelopments = executiveSummary.positiveDevelopments,
        mainConcerns = executiveSummary.mainConcerns,
    ),
    playerDevelopmentSummary = PlayerDevelopmentSummary(
        overallSummary = playerDevelopmentSummary.overallSummary,
        teamDevelopmentPriorities = playerDevelopmentSummary.teamDevelopmentPriorities.map { it.toAnalysisEvidence() },
        players = playerDevelopmentSummary.players.map { it.toAnalysisEvidence() },
    ),
    patterns = patterns.map { it.toAnalysisEvidence() },
    priorities = priorities.map { it.toAnalysisEvidence() },
    recommendations = recommendations.map { it.toAnalysisEvidence() },
    uncertainties = uncertainties.map { it.toAnalysisEvidence() },
    match = categoryAnalysis.match.toMatchCategoryAnalysis(),
)

private fun AnalysisEvidenceDto.toAnalysisEvidence() = AnalysisEvidence(
    title = title,
    description = description,
    relatedEventIds = relatedEventIds,
    relatedTranscriptionIds = relatedTranscriptionIds,
    relatedPlayerNames = relatedPlayerNames,
)

private fun MatchCategoryDto.toMatchCategoryAnalysis() = MatchCategoryAnalysis(
    summary = summary.toMatchSummary(),
    phaseComparisons = phaseComparisons.map { it.toAnalysisEvidence() },
    playerFollowUp = playerFollowUp.map { it.toAnalysisEvidence() },
    firstHalf = firstHalf.map { it.toMatchPhase() },
    halftime = halftime.map { it.toMatchPhase() },
    secondHalf = secondHalf.map { it.toMatchPhase() },
    evaluation = evaluation.map { it.toMatchPhase() },
)

private fun MatchSummaryDto.toMatchSummary() = MatchSummary(
    overallMatchAssessment = overallMatchAssessment,
    matchStory = matchStory,
    mainStrengths = mainStrengths,
    mainDevelopmentAreas = mainDevelopmentAreas,
    recommendedFollowUp = recommendedFollowUp.map { it.toAnalysisEvidence() },
)

private fun MatchPhaseDto.toMatchPhase() = MatchPhase(
    subcategory = subcategory,
    summary = summary,
    findings = findings.map { it.toAnalysisEvidence() },
    uncertainties = uncertainties.map { it.toAnalysisEvidence() },
)
