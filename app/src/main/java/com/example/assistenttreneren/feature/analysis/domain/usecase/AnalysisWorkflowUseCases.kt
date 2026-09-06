package com.example.assistenttreneren.feature.analysis.domain.usecase

import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowRepository
import javax.inject.Inject

class GetAnalysisCandidatesUseCase @Inject constructor(private val repository: AnalysisWorkflowRepository) { suspend operator fun invoke() = repository.getCandidates() }
class StartAnalysisUseCase @Inject constructor(private val repository: AnalysisWorkflowRepository) { suspend operator fun invoke(activityId: String) = repository.startAnalysis(activityId) }
class GetAnalysisUseCase @Inject constructor(private val repository: AnalysisWorkflowRepository) { suspend operator fun invoke(analysisId: String) = repository.getAnalysis(analysisId) }
