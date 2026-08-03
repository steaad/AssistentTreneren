package com.example.assistenttreneren.feature.transcription.domain.model

data class TranscriptionReview(
    val activityId: String,
    val recordings: List<TranscriptionRecording>,
)

data class TranscriptionRecording(
    val recordingId: String,
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
