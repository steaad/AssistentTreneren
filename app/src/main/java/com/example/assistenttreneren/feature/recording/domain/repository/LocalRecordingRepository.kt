package com.example.assistenttreneren.feature.recording.domain.repository

import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import kotlinx.coroutines.flow.Flow

interface LocalRecordingRepository {
    fun observeRecordingsForActivity(activityId: String): Flow<List<RecordingSession>>

    suspend fun getRecording(recordingId: String): RecordingSession?

    suspend fun saveRecording(recordingSession: RecordingSession)
}
