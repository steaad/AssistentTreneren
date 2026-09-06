package com.example.assistenttreneren.feature.analysis.data.mapper

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisCandidateDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisJobDto
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus

fun AnalysisCandidateDto.toAnalysisCandidate() = AnalysisCandidate(activityId, title, AnalysisCandidateState.valueOf(state), message, latestAnalysis?.toAnalysisJob())
fun AnalysisJobDto.toAnalysisJob() = AnalysisJob(analysisId, activityId, AnalysisJobStatus.valueOf(status), schemaVersion, promptVersion, model, createdAt, completedAt, errorMessage, result)
