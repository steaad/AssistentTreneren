package com.example.assistenttreneren.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.assistenttreneren.core.database.entity.AnalysisMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisMetadataDao {
    @Upsert
    suspend fun upsertAnalysisMetadata(metadata: AnalysisMetadataEntity)

    @Upsert
    suspend fun upsertAnalysisMetadata(metadata: List<AnalysisMetadataEntity>)

    @Query("SELECT * FROM analysis_metadata ORDER BY createdAtMillis DESC")
    fun observeAnalysisMetadata(): Flow<List<AnalysisMetadataEntity>>

    @Query("SELECT * FROM analysis_metadata WHERE analysisId = :analysisId")
    suspend fun getAnalysisMetadata(analysisId: String): AnalysisMetadataEntity?
}
