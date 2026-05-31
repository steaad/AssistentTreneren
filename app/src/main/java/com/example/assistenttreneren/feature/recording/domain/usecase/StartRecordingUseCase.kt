package com.example.assistenttreneren.feature.recording.domain.usecase

import com.example.assistenttreneren.feature.recording.domain.repository.RecordingRepository
import javax.inject.Inject

class StartRecordingUseCase @Inject constructor(
    private val recordingRepository: RecordingRepository,
) {
    operator fun invoke(
        category: String,
        subCategory: String,
        activityId: String?,
    ) {
        recordingRepository.startRecording(
            category = category,
            subCategory = subCategory,
            activityId = activityId,
        )
    }
}
