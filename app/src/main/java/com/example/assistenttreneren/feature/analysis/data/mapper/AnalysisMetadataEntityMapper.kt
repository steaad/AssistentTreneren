package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.core.database.entity.AnalysisMetadataEntity
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisMetadata
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisStatus

fun AnalysisMetadataEntity.toAnalysisMetadata(): AnalysisMetadata =
    AnalysisMetadata(
        analysisId = analysisId,
        activityId = activityId,
        title = title,
        activityCategory = activityCategory,
        status = status.toAnalysisStatus(),
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

fun AnalysisMetadata.toAnalysisMetadataEntity(): AnalysisMetadataEntity =
    AnalysisMetadataEntity(
        analysisId = analysisId,
        activityId = activityId,
        title = title,
        activityCategory = activityCategory,
        status = status.name,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

private fun String.toAnalysisStatus(): AnalysisStatus =
    runCatching { AnalysisStatus.valueOf(this) }
        .getOrDefault(AnalysisStatus.Failed)
