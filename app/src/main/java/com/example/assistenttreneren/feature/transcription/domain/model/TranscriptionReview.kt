package com.example.assistenttreneren.feature.transcription.domain.model

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
    val manuallyEdited: Boolean,
    val matchPeriod: String?,
    val matchStartMillis: Long?,
    val matchEndMillis: Long?,
)

data class TranscriptionEventIssue(
    val issueId: String,
    val issueType: String,
    val candidateText: String?,
    val contextBefore: String?,
    val contextAfter: String?,
    val startMillis: Long?,
    val endMillis: Long?,
)

data class TranscriptionEventInput(
    val text: String,
    val startMillis: Long,
    val endMillis: Long,
)
