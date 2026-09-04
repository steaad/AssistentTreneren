package com.example.assistenttreneren.feature.transcription.data.repository

import com.example.assistenttreneren.feature.transcription.data.mapper.toRequestDto
import com.example.assistenttreneren.feature.transcription.data.mapper.toTranscriptionReview
import com.example.assistenttreneren.feature.transcription.data.remote.TranscriptionReviewApi
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionReview
import com.example.assistenttreneren.feature.transcription.domain.model.isValid
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewError
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewRepository
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class TranscriptionReviewRepositoryImpl @Inject constructor(
    private val api: TranscriptionReviewApi,
) : TranscriptionReviewRepository {
    override suspend fun getReview(activityId: String): TranscriptionReviewResult<TranscriptionReview> = request {
        api.getReview(activityId).toTranscriptionReview()
    }

    override suspend fun resolveIssue(issueId: String, input: TranscriptionEventInput) = requestUnit(issueId, input) { api.resolveIssue(issueId, input.toRequestDto()) }
    override suspend fun dismissIssue(issueId: String) = requestUnit(issueId) { api.dismissIssue(issueId) }
    override suspend fun updateEvent(eventId: String, input: TranscriptionEventInput) = requestUnit(eventId, input) { api.updateEvent(eventId, input.toRequestDto()) }
    override suspend fun deleteEvent(eventId: String) = requestUnit(eventId) { api.deleteEvent(eventId) }

    private suspend fun requestUnit(id: String, block: suspend () -> Unit): TranscriptionReviewResult<Unit> {
        if (id.isBlank()) return TranscriptionReviewResult.Failure(TranscriptionReviewError.InvalidInput)
        return request(block)
    }

    private suspend fun requestUnit(id: String, input: TranscriptionEventInput, block: suspend () -> Unit): TranscriptionReviewResult<Unit> {
        if (id.isBlank() || !input.isValid()) return TranscriptionReviewResult.Failure(TranscriptionReviewError.InvalidInput)
        return request(block)
    }

    private suspend fun <T> request(block: suspend () -> T): TranscriptionReviewResult<T> = withContext(Dispatchers.IO) {
        try { TranscriptionReviewResult.Success(block())
        } catch (exception: CancellationException) { throw exception
        } catch (exception: HttpException) { TranscriptionReviewResult.Failure(exception.toError())
        } catch (exception: IOException) { TranscriptionReviewResult.Failure(TranscriptionReviewError.NetworkUnavailable)
        } catch (exception: SerializationException) { TranscriptionReviewResult.Failure(TranscriptionReviewError.InvalidServerResponse)
        } catch (exception: IllegalArgumentException) { TranscriptionReviewResult.Failure(TranscriptionReviewError.InvalidServerResponse)
        } catch (exception: Exception) { TranscriptionReviewResult.Failure(TranscriptionReviewError.Unexpected(exception.message)) }
    }

    private fun HttpException.toError(): TranscriptionReviewError = when (code()) {
        400, 422 -> TranscriptionReviewError.InvalidInput
        401, 403 -> TranscriptionReviewError.Unauthorized
        404 -> TranscriptionReviewError.NotFound
        in 500..599 -> TranscriptionReviewError.ServerError(code())
        else -> TranscriptionReviewError.Unexpected(message())
    }
}
