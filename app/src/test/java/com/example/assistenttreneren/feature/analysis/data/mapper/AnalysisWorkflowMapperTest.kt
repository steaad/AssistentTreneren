package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisCandidateDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisJobDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalysisWorkflowMapperTest {
    @Test
    fun `maps candidate and latest analysis from backend contract`() {
        val candidate = AnalysisCandidateDto(
            activityId = "activity-1",
            title = "Kamp mot Nordstrand",
            state = "COMPLETED",
            message = "Analyse klar",
            latestAnalysis = AnalysisJobDto(
                analysisId = "analysis-1",
                activityId = "activity-1",
                status = "COMPLETED",
                schemaVersion = "v1",
                promptVersion = "prompt-1",
                model = "model-1",
                createdAt = "2026-01-01T00:00:00Z",
            ),
        )

        val mapped = candidate.toAnalysisCandidate()

        assertEquals(AnalysisCandidateState.COMPLETED, mapped.state)
        assertEquals(AnalysisJobStatus.COMPLETED, mapped.latestAnalysis?.status)
        assertEquals("analysis-1", mapped.latestAnalysis?.analysisId)
    }

    @Test
    fun `parses candidate latest analysis without activity id`() {
        val dto = Json.decodeFromString<AnalysisCandidateDto>(
            """{"activityId":"activity-1","title":"Kamp","state":"COMPLETED","message":"Analyse klar","latestAnalysis":{"analysisId":"analysis-1","status":"COMPLETED","schemaVersion":"1.0","promptVersion":"match-analysis-v1","model":"gpt-5.6-sol","createdAt":"2026-09-06T10:00:00Z","completedAt":"2026-09-06T10:00:15Z"}}""",
        )

        assertEquals("analysis-1", dto.toAnalysisCandidate().latestAnalysis?.analysisId)
    }

    @Test
    fun `maps no audio recordings candidate state`() {
        val candidate = AnalysisCandidateDto(
            activityId = "activity-1",
            title = "Kamp med video",
            state = "NO_AUDIO_RECORDINGS",
            message = "Aktiviteten har ingen lydopptak å analysere.",
        )

        assertEquals(AnalysisCandidateState.NO_AUDIO_RECORDINGS, candidate.toAnalysisCandidate().state)
    }
}
