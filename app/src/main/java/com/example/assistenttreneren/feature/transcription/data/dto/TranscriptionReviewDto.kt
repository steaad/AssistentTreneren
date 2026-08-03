package com.example.assistenttreneren.feature.transcription.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionReviewDto(
    val activityId: String,
    val recordings: List<TranscriptionRecordingDto> = emptyList(),
)

@Serializable
data class TranscriptionRecordingDto(
    val recordingId: String,
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
    val manuallyEdited: Boolean = false,
)

@Serializable
data class TranscriptionEventIssueDto(
    val issueId: String,
    val issueType: String,
    val candidateText: String? = null,
    val contextBefore: String? = null,
    val contextAfter: String? = null,
    val startMillis: Long? = null,
    val endMillis: Long? = null,
)

@Serializable
data class TranscriptionEventRequestDto(
    val text: String,
    val startMillis: Long,
    val endMillis: Long,
)
