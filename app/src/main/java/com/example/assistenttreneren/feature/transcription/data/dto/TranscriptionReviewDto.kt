package com.example.assistenttreneren.feature.transcription.data.dto

import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventType
import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionReviewDto(
    val activityId: String,
    val recordings: List<TranscriptionRecordingDto> = emptyList(),
)

@Serializable
data class TranscriptionRecordingDto(
    val recordingId: String,
    val backendRecordingId: String,
    val filename: String,
    val mediaType: String,
    val category: String,
    val subCategory: String,
    val matchPeriod: String? = null,
    val matchClockStartMillis: Long? = null,
    val matchClockEndMillis: Long? = null,
    val transcriptionId: String,
    val transcriptText: String = "",
    val events: List<TranscriptionEventDto> = emptyList(),
    val issues: List<TranscriptionEventIssueDto> = emptyList(),
)

@Serializable
data class TranscriptionEventDto(
    val eventId: String,
    val text: String,
    val startMillis: Long,
    val endMillis: Long,
    val eventType: TranscriptionEventType = TranscriptionEventType.OBSERVATION,
    val playerOutName: String? = null,
    val playerInName: String? = null,
    val playerNames: List<String> = emptyList(),
    val manuallyEdited: Boolean = false,
    val matchPeriod: String? = null,
    val matchStartMillis: Long? = null,
    val matchEndMillis: Long? = null,
)

@Serializable
data class TranscriptionEventIssueDto(
    val issueId: String,
    val issueType: String,
    val relatedEventId: String? = null,
    val candidateText: String? = null,
    val message: String? = null,
    val excerpt: String? = null,
    val startMillis: Long? = null,
    val endMillis: Long? = null,
)

@Serializable
data class TranscriptionEventRequestDto(
    val text: String,
    val startMillis: Long,
    val endMillis: Long,
    val eventType: TranscriptionEventType? = null,
    val playerOutName: String? = null,
    val playerInName: String? = null,
    val playerNames: List<String>? = null,
)
