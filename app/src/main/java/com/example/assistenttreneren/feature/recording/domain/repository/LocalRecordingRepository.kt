package com.example.assistenttreneren.feature.recording.domain.repository

import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.model.RecordingUploadStatus
import kotlinx.coroutines.flow.Flow

interface LocalRecordingRepository {
    fun observeRecordingsForActivity(activityId: String): Flow<List<RecordingSession>>

    fun observeAvailableRecordingsForActivity(activityId: String): Flow<List<RecordingSession>>

    suspend fun getRecording(recordingId: String): RecordingSession?

    suspend fun saveRecording(recordingSession: RecordingSession)

    suspend fun updateUploadStatus(recordingId: String, uploadStatus: RecordingUploadStatus)
}
