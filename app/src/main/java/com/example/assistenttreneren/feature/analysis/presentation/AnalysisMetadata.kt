package com.example.assistenttreneren.feature.analysis.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
internal fun AnalysisMetadataCard(analysis: AnalysisJob) {
    val nowMillis = remember(analysis.analysisId, analysis.status) { mutableLongStateOf(System.currentTimeMillis()) }
    var currentTimeMillis by nowMillis

    LaunchedEffect(analysis.analysisId, analysis.status) {
        if (analysis.status in setOf(AnalysisJobStatus.QUEUED, AnalysisJobStatus.PROCESSING)) {
            while (true) {
                delay(1_000L)
                currentTimeMillis = System.currentTimeMillis()
            }
        }
    }

    val statusText = analysis.statusMetadataText(currentTimeMillis)
    val dataBasisText = analysis.dataBasisText()
    if (statusText == null && dataBasisText == null) return

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            statusText?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            dataBasisText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun AnalysisJob.statusMetadataText(nowMillis: Long = System.currentTimeMillis()): String? =
    when (status) {
        AnalysisJobStatus.QUEUED,
        AnalysisJobStatus.PROCESSING,
        -> startedAt?.toEpochMillisOrNull()?.let { startedAtMillis ->
            "Analyse pågår · ${formatDuration((nowMillis - startedAtMillis).coerceAtLeast(0L))}"
        }

        AnalysisJobStatus.COMPLETED -> {
            val category = activityCategory ?: "Analyse"
            val completedTime = completedAt?.toLocalTimeOrNull()
            val duration = processingDurationMillis ?: durationFromTimestamps()
            listOfNotNull(
                category,
                completedTime?.let { "Fullført $it" },
                duration?.let { "Generert på ${formatDuration(it)}" },
            ).joinToString(" · ")
        }

        AnalysisJobStatus.FAILED -> null
    }

internal fun AnalysisJob.dataBasisText(): String? = dataBasis?.let {
    "${it.recordingCount} ${if (it.recordingCount == 1) "opptak" else "opptak"} · " +
        "${it.eventCount} ${if (it.eventCount == 1) "registrert hendelse" else "registrerte hendelser"}"
}

private fun AnalysisJob.durationFromTimestamps(): Long? {
    val startedAtMillis = startedAt?.toEpochMillisOrNull() ?: return null
    val completedAtMillis = completedAt?.toEpochMillisOrNull() ?: return null
    return (completedAtMillis - startedAtMillis).coerceAtLeast(0L)
}

private fun String.toEpochMillisOrNull(): Long? = runCatching { Instant.parse(this).toEpochMilli() }.getOrNull()

private fun String.toLocalTimeOrNull(): String? = runCatching {
    Instant.parse(this).atZone(ZoneId.systemDefault()).format(timeFormatter)
}.getOrNull()

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (minutes > 0L) "$minutes min $seconds sek" else "$seconds sek"
}

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
