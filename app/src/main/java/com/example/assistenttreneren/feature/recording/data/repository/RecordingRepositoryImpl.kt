package com.example.assistenttreneren.feature.recording.data.repository

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.assistenttreneren.core.media.AudioRecordingService
import com.example.assistenttreneren.core.media.AudioRecordingStateHolder
import com.example.assistenttreneren.feature.recording.domain.model.RecordingStatus
import com.example.assistenttreneren.feature.recording.domain.repository.RecordingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class RecordingRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val stateHolder: AudioRecordingStateHolder,
) : RecordingRepository {
    override val recordingStatus: StateFlow<RecordingStatus> = stateHolder.recordingStatus

    override fun startRecording(
        category: String,
        subCategory: String,
        activityId: String?,
        matchPeriod: String?,
        matchClockStartMillis: Long?,
    ) {
        ContextCompat.startForegroundService(
            context,
            AudioRecordingService.startIntent(
                context = context,
                category = category,
                subCategory = subCategory,
                activityId = activityId,
                matchPeriod = matchPeriod,
                matchClockStartMillis = matchClockStartMillis,
            ),
        )
    }

    override fun stopRecording() {
        context.startService(AudioRecordingService.stopIntent(context))
    }
}
