package com.example.assistenttreneren.feature.recording.domain.model

sealed interface RecordingStatus {
    data object Idle : RecordingStatus

    data class Recording(
        val recordingId: String,
        val displayName: String,
        val mediaType: RecordingMediaType,
        val startedAtMillis: Long,
    ) : RecordingStatus

    data class Completed(
        val session: RecordingSession,
    ) : RecordingStatus

    data class Error(
        val message: String,
    ) : RecordingStatus
}
