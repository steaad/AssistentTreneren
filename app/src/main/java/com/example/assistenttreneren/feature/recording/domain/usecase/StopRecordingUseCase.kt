package com.example.assistenttreneren.feature.recording.domain.usecase

import com.example.assistenttreneren.feature.recording.domain.repository.RecordingRepository
import javax.inject.Inject

class StopRecordingUseCase @Inject constructor(
    private val recordingRepository: RecordingRepository,
) {
    operator fun invoke() {
        recordingRepository.stopRecording()
    }
}
