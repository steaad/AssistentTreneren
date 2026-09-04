package com.example.assistenttreneren.feature.transcription.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TranscriptionEventType {
    OBSERVATION,
    STAT,
    SUBSTITUTION,
    STARTING_LINEUP,
}

data class TranscriptionReview(
    val activityId: String,
    val recordings: List<TranscriptionRecording>,
)

data class TranscriptionRecording(
    val recordingId: String,
    val backendRecordingId: String,
    val filename: String,
    val mediaType: String,
    val category: String,
    val subCategory: String,
    val matchPeriod: String?,
    val matchClockStartMillis: Long?,
    val matchClockEndMillis: Long?,
    val transcriptionId: String,
    val transcriptText: String,
    val events: List<TranscriptionEvent>,
    val issues: List<TranscriptionEventIssue>,
)

data class TranscriptionEvent(
    val eventId: String,
    val text: String,
    val startMillis: Long,
    val endMillis: Long,
    val eventType: TranscriptionEventType,
    val playerOutName: String?,
    val playerInName: String?,
    val playerNames: List<String>,
    val manuallyEdited: Boolean,
    val matchPeriod: String?,
    val matchStartMillis: Long?,
    val matchEndMillis: Long?,
)

data class TranscriptionEventIssue(
    val issueId: String,
    val issueType: String,
    val relatedEventId: String? = null,
    val candidateText: String?,
    val message: String?,
    val excerpt: String?,
    val startMillis: Long?,
    val endMillis: Long?,
)

data class TranscriptionEventInput(
    val text: String,
    val startMillis: Long,
    val endMillis: Long,
    val eventType: TranscriptionEventType? = null,
    val playerOutName: String? = null,
    val playerInName: String? = null,
    val playerNames: List<String>? = null,
)

fun TranscriptionEventInput.isValid(): Boolean =
    text.isNotBlank() &&
        startMillis >= 0 &&
        endMillis >= startMillis &&
        (eventType != TranscriptionEventType.SUBSTITUTION ||
            (!playerOutName.isNullOrBlank() && !playerInName.isNullOrBlank())) &&
        (eventType != TranscriptionEventType.STARTING_LINEUP ||
            !playerNames.isNullOrEmpty())
