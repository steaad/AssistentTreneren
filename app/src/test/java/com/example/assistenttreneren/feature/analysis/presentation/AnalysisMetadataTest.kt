package com.example.assistenttreneren.feature.analysis.presentation

import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisDataBasis
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalysisMetadataTest {
    @Test
    fun `formats completed analysis metadata and data basis`() {
        val analysis = analysisJob(
            status = AnalysisJobStatus.COMPLETED,
            completedAt = "2026-09-07T12:32:00Z",
            processingDurationMillis = 83_000L,
            dataBasis = AnalysisDataBasis(recordingCount = 2, eventCount = 14),
        )

        val metadata = analysis.statusMetadataText()

        assertEquals("Kamp · Fullført 14:32 · Generert på 1 min 23 sek", metadata)
        assertEquals("2 opptak · 14 registrerte hendelser", analysis.dataBasisText())
    }

    @Test
    fun `formats elapsed processing time from started at`() {
        val analysis = analysisJob(
            status = AnalysisJobStatus.PROCESSING,
            startedAt = "2026-09-07T12:00:00Z",
        )

        assertEquals("Analyse pågår · 1 min 23 sek", analysis.statusMetadataText(nowMillis = 1_788_782_483_000L))
    }

    private fun analysisJob(
        status: AnalysisJobStatus,
        startedAt: String? = "2026-09-07T12:00:00Z",
        completedAt: String? = null,
        processingDurationMillis: Long? = null,
        dataBasis: AnalysisDataBasis? = null,
    ) = AnalysisJob(
        analysisId = "analysis-1",
        activityId = "activity-1",
        status = status,
        schemaVersion = "1.0",
        promptVersion = "match-analysis-v1",
        model = "test",
        createdAt = "2026-09-07T12:00:00Z",
        completedAt = completedAt,
        errorMessage = null,
        result = null,
        activityCategory = "Kamp",
        startedAt = startedAt,
        processingDurationMillis = processingDurationMillis,
        dataBasis = dataBasis,
    )
}
