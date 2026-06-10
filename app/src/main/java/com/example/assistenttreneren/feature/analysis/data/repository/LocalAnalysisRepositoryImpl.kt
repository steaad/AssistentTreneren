package com.example.assistenttreneren.feature.analysis.data.repository

import com.example.assistenttreneren.core.database.dao.AnalysisMetadataDao
import com.example.assistenttreneren.feature.analysis.data.mapper.toAnalysisMetadata
import com.example.assistenttreneren.feature.analysis.data.mapper.toAnalysisMetadataEntity
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisMetadata
import com.example.assistenttreneren.feature.analysis.domain.repository.LocalAnalysisRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalAnalysisRepositoryImpl @Inject constructor(
    private val analysisMetadataDao: AnalysisMetadataDao,
) : LocalAnalysisRepository {
    override fun observeAnalysisMetadata(): Flow<List<AnalysisMetadata>> =
        analysisMetadataDao.observeAnalysisMetadata()
            .map { metadata -> metadata.map { it.toAnalysisMetadata() } }

    override suspend fun getAnalysisMetadata(analysisId: String): AnalysisMetadata? =
        analysisMetadataDao.getAnalysisMetadata(analysisId)?.toAnalysisMetadata()

    override suspend fun saveAnalysisMetadata(metadata: AnalysisMetadata) {
        analysisMetadataDao.upsertAnalysisMetadata(metadata.toAnalysisMetadataEntity())
    }

    override suspend fun saveAnalysisMetadata(metadata: List<AnalysisMetadata>) {
        analysisMetadataDao.upsertAnalysisMetadata(
            metadata.map { it.toAnalysisMetadataEntity() },
        )
    }
}
