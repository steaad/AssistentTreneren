package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisCandidateDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisDataBasisDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisJobDto
import com.example.assistenttreneren.feature.analysis.data.dto.ActivityAnalysisSummaryDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisDataBasis
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisSummary

fun AnalysisCandidateDto.toAnalysisCandidate() = AnalysisCandidate(activityId, title, AnalysisCandidateState.valueOf(state), message, latestAnalysis?.toAnalysisSummary())
fun AnalysisJobDto.toAnalysisJob() = AnalysisJob(
    analysisId = analysisId,
    activityId = activityId.orEmpty(),
    status = AnalysisJobStatus.valueOf(status),
    schemaVersion = schemaVersion,
    promptVersion = promptVersion,
    model = model,
    createdAt = createdAt,
    completedAt = completedAt,
    errorMessage = errorMessage,
    result = result,
    activityCategory = activityCategory,
    startedAt = startedAt,
    processingDurationMillis = processingDurationMillis,
    dataBasis = dataBasis?.toAnalysisDataBasis(),
)

private fun AnalysisDataBasisDto.toAnalysisDataBasis() = AnalysisDataBasis(
    recordingCount = recordingCount,
    eventCount = eventCount,
)

fun ActivityAnalysisSummaryDto.toAnalysisSummary() = AnalysisSummary(
    analysisId = analysisId,
    version = version,
    inputRevision = inputRevision,
    status = AnalysisJobStatus.valueOf(status),
    createdAt = createdAt,
    completedAt = completedAt,
)
