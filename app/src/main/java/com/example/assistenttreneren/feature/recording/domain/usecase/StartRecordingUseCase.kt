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
        matchPeriod: String? = null,
        matchClockStartMillis: Long? = null,
    ) {
        recordingRepository.startRecording(
            category = category,
            subCategory = subCategory,
            activityId = activityId,
            matchPeriod = matchPeriod,
            matchClockStartMillis = matchClockStartMillis,
        )
    }
}
