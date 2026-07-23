package com.example.assistenttreneren.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.assistenttreneren.core.database.entity.LocalRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalRecordingDao {
    @Upsert
    suspend fun upsertRecording(recording: LocalRecordingEntity)

    @Query(
        """
        SELECT * FROM local_recordings
        WHERE activityId = :activityId
        ORDER BY createdAtMillis DESC
        """,
    )
    fun observeRecordingsForActivity(activityId: String): Flow<List<LocalRecordingEntity>>

    @Query(
        """
        SELECT recording.*
        FROM local_recordings AS recording
        WHERE recording.activityId = :activityId
          AND recording.uploadStatus = 'AvailableForUpload'
          AND NOT EXISTS (
              SELECT 1
              FROM upload_jobs AS uploadJob
              WHERE uploadJob.recordingId = recording.recordingId
                AND uploadJob.status = 'Completed'
          )
        ORDER BY recording.createdAtMillis DESC
        """,
    )
    fun observeAvailableRecordingsForActivity(activityId: String): Flow<List<LocalRecordingEntity>>

    @Query("SELECT * FROM local_recordings WHERE recordingId = :recordingId")
    suspend fun getRecording(recordingId: String): LocalRecordingEntity?

    @Query("UPDATE local_recordings SET uploadStatus = :uploadStatus WHERE recordingId = :recordingId")
    suspend fun updateUploadStatus(recordingId: String, uploadStatus: String)
}
