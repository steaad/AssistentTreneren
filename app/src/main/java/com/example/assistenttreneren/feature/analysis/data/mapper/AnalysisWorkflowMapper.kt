package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisCandidateDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisDataBasisDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisJobDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisDataBasis
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus

fun AnalysisCandidateDto.toAnalysisCandidate() = AnalysisCandidate(activityId, title, AnalysisCandidateState.valueOf(state), message, latestAnalysis?.toAnalysisJob())
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
