package com.example.assistenttreneren.feature.transcription.domain.usecase

import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewRepository
import javax.inject.Inject

class GetTranscriptionReviewUseCase @Inject constructor(
    private val repository: TranscriptionReviewRepository,
) { suspend operator fun invoke(activityId: String) = repository.getReview(activityId) }

class ResolveTranscriptionIssueUseCase @Inject constructor(
    private val repository: TranscriptionReviewRepository,
) { suspend operator fun invoke(issueId: String, input: TranscriptionEventInput) = repository.resolveIssue(issueId, input) }

class DismissTranscriptionIssueUseCase @Inject constructor(
    private val repository: TranscriptionReviewRepository,
) { suspend operator fun invoke(issueId: String) = repository.dismissIssue(issueId) }

class UpdateTranscriptionEventUseCase @Inject constructor(
    private val repository: TranscriptionReviewRepository,
) { suspend operator fun invoke(eventId: String, input: TranscriptionEventInput) = repository.updateEvent(eventId, input) }

class DeleteTranscriptionEventUseCase @Inject constructor(
    private val repository: TranscriptionReviewRepository,
) { suspend operator fun invoke(eventId: String) = repository.deleteEvent(eventId) }
