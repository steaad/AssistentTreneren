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

    @Query("SELECT * FROM local_recordings WHERE recordingId = :recordingId")
    suspend fun getRecording(recordingId: String): LocalRecordingEntity?
}
