package com.example.assistenttreneren.feature.analysis.data.repository

import com.example.assistenttreneren.feature.analysis.data.mapper.toAnalysisCandidate
import com.example.assistenttreneren.feature.analysis.data.mapper.toAnalysisJob
import com.example.assistenttreneren.feature.analysis.data.remote.AnalysisApi
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowError
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowRepository
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class AnalysisWorkflowRepositoryImpl @Inject constructor(private val api: AnalysisApi) : AnalysisWorkflowRepository {
    override suspend fun getCandidates() = request { api.getAnalysisCandidates().map { it.toAnalysisCandidate() } }
    override suspend fun startAnalysis(activityId: String) = requestForId(activityId) { api.startAnalysis(activityId).toAnalysisJob() }
    override suspend fun getAnalysis(analysisId: String) = requestForId(analysisId) { api.getAnalysis(analysisId).toAnalysisJob() }

    private suspend fun <T> requestForId(id: String, block: suspend () -> T): AnalysisWorkflowResult<T> =
        if (id.isBlank()) AnalysisWorkflowResult.Failure(AnalysisWorkflowError.InvalidInput) else request(block)

    private suspend fun <T> request(block: suspend () -> T): AnalysisWorkflowResult<T> = withContext(Dispatchers.IO) {
        try { AnalysisWorkflowResult.Success(block())
        } catch (e: CancellationException) { throw e
        } catch (e: HttpException) { AnalysisWorkflowResult.Failure(e.toError())
        } catch (e: IOException) { AnalysisWorkflowResult.Failure(AnalysisWorkflowError.NetworkUnavailable)
        } catch (e: SerializationException) { AnalysisWorkflowResult.Failure(AnalysisWorkflowError.InvalidServerResponse)
        } catch (e: IllegalArgumentException) { AnalysisWorkflowResult.Failure(AnalysisWorkflowError.InvalidServerResponse)
        } catch (e: Exception) { AnalysisWorkflowResult.Failure(AnalysisWorkflowError.Unexpected(e.message)) }
    }

    private fun HttpException.toError() = when (code()) {
        409 -> AnalysisWorkflowError.Conflict(message())
        400, 422 -> AnalysisWorkflowError.InvalidInput
        401, 403 -> AnalysisWorkflowError.Unauthorized
        404 -> AnalysisWorkflowError.NotFound
        in 500..599 -> AnalysisWorkflowError.ServerError(code())
        else -> AnalysisWorkflowError.Unexpected(message())
    }
}
