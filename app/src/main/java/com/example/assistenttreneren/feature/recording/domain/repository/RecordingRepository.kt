package com.example.assistenttreneren.feature.recording.domain.repository

import com.example.assistenttreneren.feature.recording.domain.model.RecordingStatus
import kotlinx.coroutines.flow.StateFlow

interface RecordingRepository {
    val recordingStatus: StateFlow<RecordingStatus>

    fun startRecording(
        category: String,
        subCategory: String,
        activityId: String?,
        matchPeriod: String? = null,
        matchClockStartMillis: Long? = null,
    )

    fun stopRecording()
}
