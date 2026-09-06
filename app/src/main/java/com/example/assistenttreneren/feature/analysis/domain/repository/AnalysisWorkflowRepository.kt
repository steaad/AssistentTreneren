package com.example.assistenttreneren.feature.analysis.domain.repository

import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob

interface AnalysisWorkflowRepository {
    suspend fun getCandidates(): AnalysisWorkflowResult<List<AnalysisCandidate>>
    suspend fun startAnalysis(activityId: String): AnalysisWorkflowResult<AnalysisJob>
    suspend fun getAnalysis(analysisId: String): AnalysisWorkflowResult<AnalysisJob>
}

sealed interface AnalysisWorkflowResult<out T> {
    data class Success<T>(val data: T) : AnalysisWorkflowResult<T>
    data class Failure(val error: AnalysisWorkflowError) : AnalysisWorkflowResult<Nothing>
}

sealed interface AnalysisWorkflowError {
    data object InvalidInput : AnalysisWorkflowError
    data object NetworkUnavailable : AnalysisWorkflowError
    data object InvalidServerResponse : AnalysisWorkflowError
    data object Unauthorized : AnalysisWorkflowError
    data object NotFound : AnalysisWorkflowError
    data class Conflict(val message: String?) : AnalysisWorkflowError
    data class ServerError(val code: Int) : AnalysisWorkflowError
    data class Unexpected(val message: String?) : AnalysisWorkflowError
}
