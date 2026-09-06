package com.example.assistenttreneren.feature.analysis.data.remote

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisSummaryDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisCandidateDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisJobDto
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface AnalysisApi {
    @GET("api/activities/analysis-candidates")
    suspend fun getAnalysisCandidates(): List<AnalysisCandidateDto>

    @POST("api/activities/{activityId}/analyses")
    suspend fun startAnalysis(@Path("activityId") activityId: String): AnalysisJobDto

    @GET("api/analyses")
    suspend fun getAnalyses(): List<AnalysisSummaryDto>

    @GET("api/analyses/{analysisId}")
    suspend fun getAnalysis(
        @Path("analysisId") analysisId: String,
    ): AnalysisJobDto
}
