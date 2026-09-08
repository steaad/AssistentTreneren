package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisEvidenceDto
import com.example.assistenttreneren.feature.analysis.data.dto.ExecutiveSummaryDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchAnalysisResultDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchCategoryAnalysisDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchCategoryDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchPhaseDto
import com.example.assistenttreneren.feature.analysis.data.dto.MatchSummaryDto
import com.example.assistenttreneren.feature.analysis.data.dto.PlayerDevelopmentSummaryDto
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchAnalysisResultMapperTest {
    @Test
    fun `maps all match result sections and evidence references`() {
        val evidence = AnalysisEvidenceDto(
            title = "Høyt press",
            description = "Laget vant baller høyt.",
            relatedEventIds = listOf("event-1"),
            relatedTranscriptionIds = listOf("transcription-1"),
            relatedPlayerNames = listOf("Olav"),
        )
        val dto = MatchAnalysisResultDto(
            executiveSummary = ExecutiveSummaryDto("Kampanalyse", "Kort oppsummering", listOf("Tempo"), listOf("Initiativ"), listOf("Restforsvar")),
            playerDevelopmentSummary = PlayerDevelopmentSummaryDto("Utvikling", listOf(evidence), listOf(evidence)),
            patterns = listOf(evidence),
            priorities = listOf(evidence),
            recommendations = listOf(evidence),
            uncertainties = listOf(evidence),
            categoryAnalysis = MatchCategoryAnalysisDto(
                MatchCategoryDto(
                    summary = MatchSummaryDto("Vurdering", "Fortelling", listOf("Press"), listOf("Kompakthet"), listOf(evidence)),
                    phaseComparisons = listOf(evidence),
                    playerFollowUp = listOf(evidence),
                    firstHalf = listOf(MatchPhaseDto("1.omgang", "God start", listOf(evidence), emptyList())),
                    halftime = emptyList(),
                    secondHalf = emptyList(),
                    evaluation = emptyList(),
                ),
            ),
        )

        val result = dto.toMatchAnalysisResult()

        assertEquals("Kampanalyse", result.executiveSummary.title)
        assertEquals("Olav", result.patterns.single().relatedPlayerNames.single())
        assertEquals("event-1", result.priorities.single().relatedEventIds.single())
        assertEquals("1.omgang", result.match.firstHalf.single().subcategory)
        assertEquals("Høyt press", result.match.firstHalf.single().findings.single().title)
    }
}
