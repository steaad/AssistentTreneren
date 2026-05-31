package com.example.assistenttreneren.core.media

import com.example.assistenttreneren.feature.recording.domain.model.RecordingStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AudioRecordingStateHolder @Inject constructor() {
    private val _recordingStatus = MutableStateFlow<RecordingStatus>(RecordingStatus.Idle)
    val recordingStatus: StateFlow<RecordingStatus> = _recordingStatus.asStateFlow()

    fun update(status: RecordingStatus) {
        _recordingStatus.value = status
    }
}
