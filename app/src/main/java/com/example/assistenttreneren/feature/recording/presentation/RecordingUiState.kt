package com.example.assistenttreneren.feature.recording.presentation

import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType

data class RecordingUiState(
    val selectedMediaType: RecordingMediaType? = null,
    val subCategory: String = "",
    val isRecording: Boolean = false,
    val recordingStartedAtMillis: Long? = null,
    val activeDisplayName: String? = null,
    val completedRecording: RecordingSession? = null,
    val errorMessage: String? = null,
) {
    val canStartRecording: Boolean
        get() = !isRecording && subCategory.isNotBlank() && selectedMediaType != null

    val canStopRecording: Boolean
        get() = isRecording
}
