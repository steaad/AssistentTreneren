package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisDetailDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisSummaryDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisMetadata
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisStatus

fun AnalysisSummaryDto.toAnalysisMetadata(): AnalysisMetadata =
    AnalysisMetadata(
        analysisId = analysisId,
        activityId = activityId,
        title = title,
        activityCategory = activityCategory,
        status = status.toAnalysisStatusFromDto(),
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

fun AnalysisDetailDto.toAnalysisMetadata(): AnalysisMetadata =
    AnalysisMetadata(
        analysisId = analysisId,
        activityId = activityId,
        title = title,
        activityCategory = activityCategory,
        status = status.toAnalysisStatusFromDto(),
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

fun String.toAnalysisStatusFromDto(): AnalysisStatus =
    runCatching { AnalysisStatus.valueOf(this) }
        .getOrDefault(AnalysisStatus.Failed)
