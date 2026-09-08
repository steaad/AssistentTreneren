package com.example.assistenttreneren.feature.analysis.presentation

import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus

data class AnalysisUiState(
    val isLoadingCandidates: Boolean = false,
    val candidates: List<AnalysisCandidate> = emptyList(),
    val completedAnalyses: List<AnalysisCandidate> = emptyList(),
    val selectedActivityId: String? = null,
    val isStartingAnalysis: Boolean = false,
    val activeAnalysis: AnalysisJob? = null,
    val completedAnalysis: AnalysisJob? = null,
    val errorMessage: String? = null,
) {
    val selectedCandidate: AnalysisCandidate?
        get() = candidates.firstOrNull { it.activityId == selectedActivityId }

    val isAnalysisInProgress: Boolean
        get() = activeAnalysis?.status in setOf(AnalysisJobStatus.QUEUED, AnalysisJobStatus.PROCESSING)
}
