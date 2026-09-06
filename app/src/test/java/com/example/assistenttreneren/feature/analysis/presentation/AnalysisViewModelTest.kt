package com.example.assistenttreneren.feature.analysis.presentation

import com.example.assistenttreneren.MainDispatcherRule
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowRepository
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowResult
import com.example.assistenttreneren.feature.analysis.domain.usecase.GetAnalysisCandidatesUseCase
import com.example.assistenttreneren.feature.analysis.domain.usecase.GetAnalysisUseCase
import com.example.assistenttreneren.feature.analysis.domain.usecase.StartAnalysisUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `only ready candidate enables starting an analysis`() = runTest {
        val repository = FakeAnalysisWorkflowRepository(
            candidates = listOf(candidate(state = AnalysisCandidateState.REVIEW_REQUIRED)),
        )
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.startSelectedAnalysis()
        runCurrent()

        assertEquals(0, repository.startCalls)
    }

    @Test
    fun `starts a ready analysis and polls until completed`() = runTest {
        val runningJob = analysisJob(status = AnalysisJobStatus.QUEUED)
        val completedJob = analysisJob(status = AnalysisJobStatus.COMPLETED)
        val repository = FakeAnalysisWorkflowRepository(
            candidates = listOf(candidate()),
            startResult = AnalysisWorkflowResult.Success(runningJob),
            analysisResults = ArrayDeque(
                listOf(
                    AnalysisWorkflowResult.Success(analysisJob(status = AnalysisJobStatus.PROCESSING)),
                    AnalysisWorkflowResult.Success(completedJob),
                ),
            ),
        )
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.startSelectedAnalysis()
        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(1, repository.startCalls)
        assertEquals(2, repository.getAnalysisCalls)
        assertEquals(completedJob, viewModel.uiState.value.completedAnalysis)
        assertFalse(viewModel.uiState.value.isAnalysisInProgress)
    }

    @Test
    fun `refresh keeps selected activity when it remains available`() = runTest {
        val first = candidate(activityId = "first")
        val second = candidate(activityId = "second")
        val repository = FakeAnalysisWorkflowRepository(candidates = listOf(first, second))
        val viewModel = viewModel(repository)
        runCurrent()
        viewModel.onActivitySelected(second.activityId)

        viewModel.refreshCandidates()
        runCurrent()

        assertEquals(second.activityId, viewModel.uiState.value.selectedActivityId)
        assertTrue(viewModel.uiState.value.candidates.isNotEmpty())
    }

    private fun viewModel(repository: AnalysisWorkflowRepository) = AnalysisViewModel(
        GetAnalysisCandidatesUseCase(repository),
        StartAnalysisUseCase(repository),
        GetAnalysisUseCase(repository),
    )

    private class FakeAnalysisWorkflowRepository(
        private val candidates: List<AnalysisCandidate>,
        private val startResult: AnalysisWorkflowResult<AnalysisJob> = AnalysisWorkflowResult.Success(analysisJob()),
        private val analysisResults: ArrayDeque<AnalysisWorkflowResult<AnalysisJob>> = ArrayDeque(),
    ) : AnalysisWorkflowRepository {
        var startCalls = 0
        var getAnalysisCalls = 0

        override suspend fun getCandidates() = AnalysisWorkflowResult.Success(candidates)

        override suspend fun startAnalysis(activityId: String): AnalysisWorkflowResult<AnalysisJob> {
            startCalls++
            return startResult
        }

        override suspend fun getAnalysis(analysisId: String): AnalysisWorkflowResult<AnalysisJob> {
            getAnalysisCalls++
            return analysisResults.removeFirstOrNull() ?: AnalysisWorkflowResult.Success(analysisJob())
        }
    }

    private companion object {
        fun candidate(
            activityId: String = "activity-1",
            state: AnalysisCandidateState = AnalysisCandidateState.READY,
        ) = AnalysisCandidate(
            activityId = activityId,
            title = "Kamp mot Nordstrand",
            state = state,
            message = "Klar for analyse",
            latestAnalysis = null,
        )

        fun analysisJob(status: AnalysisJobStatus = AnalysisJobStatus.QUEUED) = AnalysisJob(
            analysisId = "analysis-1",
            activityId = "activity-1",
            status = status,
            schemaVersion = "1",
            promptVersion = "1",
            model = "test",
            createdAt = "2026-01-01T00:00:00Z",
            completedAt = if (status == AnalysisJobStatus.COMPLETED) "2026-01-01T00:01:00Z" else null,
            errorMessage = null,
            result = null,
        )
    }
}
