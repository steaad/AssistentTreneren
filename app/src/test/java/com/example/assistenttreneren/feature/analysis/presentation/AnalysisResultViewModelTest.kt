package com.example.assistenttreneren.feature.analysis.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.assistenttreneren.MainDispatcherRule
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowRepository
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowResult
import com.example.assistenttreneren.feature.analysis.domain.usecase.GetAnalysisUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisResultViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps completed match analysis to result state`() = runTest {
        val viewModel = AnalysisResultViewModel(
            SavedStateHandle(mapOf("analysisId" to "analysis-1")),
            GetAnalysisUseCase(FakeRepository(completedJob(matchResultJson))),
        )
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Kampanalyse mot Nord", viewModel.uiState.value.matchResult?.executiveSummary?.title)
        assertEquals("Olav", viewModel.uiState.value.matchResult?.patterns?.single()?.relatedPlayerNames?.single())
    }

    @Test
    fun `uses generic fallback for a future analysis prompt`() = runTest {
        val viewModel = AnalysisResultViewModel(
            SavedStateHandle(mapOf("analysisId" to "analysis-1")),
            GetAnalysisUseCase(FakeRepository(completedJob(matchResultJson, promptVersion = "training-analysis-v1"))),
        )
        runCurrent()

        assertNotNull(viewModel.uiState.value.unsupportedResult)
    }

    private class FakeRepository(private val job: AnalysisJob) : AnalysisWorkflowRepository {
        override suspend fun getCandidates(): AnalysisWorkflowResult<List<AnalysisCandidate>> = error("Not used")
        override suspend fun startAnalysis(activityId: String): AnalysisWorkflowResult<AnalysisJob> = error("Not used")
        override suspend fun getAnalysis(analysisId: String) = AnalysisWorkflowResult.Success(job)
    }

    private companion object {
        val matchResultJson = Json.parseToJsonElement(
            """{"executiveSummary":{"title":"Kampanalyse mot Nord","summary":"Sammendrag","keyTakeaways":["Intensitet"],"positiveDevelopments":[],"mainConcerns":[]},"playerDevelopmentSummary":{"overallSummary":"Utvikling","teamDevelopmentPriorities":[],"players":[]},"patterns":[{"title":"Press","description":"Høyt press","relatedEventIds":["event-1"],"relatedTranscriptionIds":["transcription-1"],"relatedPlayerNames":["Olav"]}],"priorities":[],"recommendations":[],"uncertainties":[],"categoryAnalysis":{"match":{"summary":{"overallMatchAssessment":"God kamp","matchStory":"Kampfortelling","mainStrengths":[],"mainDevelopmentAreas":[],"recommendedFollowUp":[]},"phaseComparisons":[],"playerFollowUp":[],"firstHalf":[],"halftime":[],"secondHalf":[],"evaluation":[]}}}""",
        )

        fun completedJob(result: kotlinx.serialization.json.JsonElement, promptVersion: String = "match-analysis-v1") = AnalysisJob(
            analysisId = "analysis-1",
            activityId = "activity-1",
            status = AnalysisJobStatus.COMPLETED,
            schemaVersion = "1.0",
            promptVersion = promptVersion,
            model = "test",
            createdAt = "2026-09-06T10:00:00Z",
            completedAt = "2026-09-06T10:01:00Z",
            errorMessage = null,
            result = result,
        )
    }
}
