package com.example.assistenttreneren.feature.recording.data.repository

import com.example.assistenttreneren.core.database.dao.LocalRecordingDao
import com.example.assistenttreneren.feature.recording.data.mapper.toLocalRecordingEntity
import com.example.assistenttreneren.feature.recording.data.mapper.toRecordingSession
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRecordingRepositoryImpl @Inject constructor(
    private val localRecordingDao: LocalRecordingDao,
) : LocalRecordingRepository {
    override fun observeRecordingsForActivity(activityId: String): Flow<List<RecordingSession>> =
        localRecordingDao.observeRecordingsForActivity(activityId)
            .map { recordings -> recordings.map { it.toRecordingSession() } }

    override suspend fun getRecording(recordingId: String): RecordingSession? =
        localRecordingDao.getRecording(recordingId)?.toRecordingSession()

    override suspend fun saveRecording(recordingSession: RecordingSession) {
        localRecordingDao.upsertRecording(recordingSession.toLocalRecordingEntity())
    }
}
