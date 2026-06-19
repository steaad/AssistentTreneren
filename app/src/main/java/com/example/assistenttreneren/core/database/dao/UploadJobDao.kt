package com.example.assistenttreneren.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.assistenttreneren.core.database.entity.UploadJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadJobDao {
    @Upsert
    suspend fun upsertUploadJob(uploadJob: UploadJobEntity)

    @Query(
        """
        SELECT * FROM upload_jobs
        WHERE activityId = :activityId
        ORDER BY updatedAtMillis DESC
        """,
    )
    fun observeUploadJobsForActivity(activityId: String): Flow<List<UploadJobEntity>>

    @Query("SELECT * FROM upload_jobs WHERE uploadJobId = :uploadJobId")
    suspend fun getUploadJob(uploadJobId: String): UploadJobEntity?

    @Query(
        """
        SELECT * FROM upload_jobs
        WHERE recordingId = :recordingId
        ORDER BY updatedAtMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestUploadJobForRecording(recordingId: String): UploadJobEntity?
}
