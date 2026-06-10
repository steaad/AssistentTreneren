package com.example.assistenttreneren.feature.analysis.data.remote

import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisDetailDto
import com.example.assistenttreneren.feature.analysis.data.dto.AnalysisSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path

interface AnalysisApi {
    @GET("api/analyses")
    suspend fun getAnalyses(): List<AnalysisSummaryDto>

    @GET("api/analyses/{analysisId}")
    suspend fun getAnalysis(
        @Path("analysisId") analysisId: String,
    ): AnalysisDetailDto
}
