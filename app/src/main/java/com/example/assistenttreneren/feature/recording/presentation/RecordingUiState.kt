package com.example.assistenttreneren.feature.recording.presentation

import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession

data class RecordingUiState(
    val subCategory: String = "",
    val isRecording: Boolean = false,
    val activeDisplayName: String? = null,
    val completedRecording: RecordingSession? = null,
    val errorMessage: String? = null,
) {
    val canStartRecording: Boolean
        get() = !isRecording && subCategory.isNotBlank()

    val canStopRecording: Boolean
        get() = isRecording
}
