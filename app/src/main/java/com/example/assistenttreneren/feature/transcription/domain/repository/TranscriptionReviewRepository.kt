package com.example.assistenttreneren.feature.transcription.domain.repository

import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionReview

interface TranscriptionReviewRepository {
    suspend fun getReview(activityId: String): TranscriptionReviewResult<TranscriptionReview>

    suspend fun resolveIssue(issueId: String, input: TranscriptionEventInput): TranscriptionReviewResult<Unit>

    suspend fun dismissIssue(issueId: String): TranscriptionReviewResult<Unit>

    suspend fun updateEvent(eventId: String, input: TranscriptionEventInput): TranscriptionReviewResult<Unit>

    suspend fun deleteEvent(eventId: String): TranscriptionReviewResult<Unit>
}

sealed interface TranscriptionReviewResult<out T> {
    data class Success<T>(val data: T) : TranscriptionReviewResult<T>
    data class Failure(val error: TranscriptionReviewError) : TranscriptionReviewResult<Nothing>
}

sealed interface TranscriptionReviewError {
    data object InvalidInput : TranscriptionReviewError
    data object InvalidServerResponse : TranscriptionReviewError
    data object NetworkUnavailable : TranscriptionReviewError
    data object NotFound : TranscriptionReviewError
    data object Unauthorized : TranscriptionReviewError
    data class ServerError(val code: Int) : TranscriptionReviewError
    data class Unexpected(val message: String?) : TranscriptionReviewError
}
