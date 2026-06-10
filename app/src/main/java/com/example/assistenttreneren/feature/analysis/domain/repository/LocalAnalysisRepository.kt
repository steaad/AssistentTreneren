package com.example.assistenttreneren.feature.analysis.domain.repository

import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisMetadata
import kotlinx.coroutines.flow.Flow

interface LocalAnalysisRepository {
    fun observeAnalysisMetadata(): Flow<List<AnalysisMetadata>>

    suspend fun getAnalysisMetadata(analysisId: String): AnalysisMetadata?

    suspend fun saveAnalysisMetadata(metadata: AnalysisMetadata)

    suspend fun saveAnalysisMetadata(metadata: List<AnalysisMetadata>)
}
