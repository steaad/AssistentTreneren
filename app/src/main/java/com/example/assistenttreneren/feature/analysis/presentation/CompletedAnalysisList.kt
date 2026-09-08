package com.example.assistenttreneren.feature.analysis.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate

@Composable
internal fun CompletedAnalysisList(
    analyses: List<AnalysisCandidate>,
    onAnalysisSelected: (String) -> Unit,
    emptyMessage: String,
) {
    if (analyses.isEmpty()) {
        Text(emptyMessage, style = MaterialTheme.typography.bodyMedium)
        return
    }

    Column {
        analyses.forEach { candidate ->
            val analysisId = candidate.latestAnalysis?.analysisId ?: return@forEach
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onAnalysisSelected(analysisId) },
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(candidate.title ?: "Analyse uten tittel", fontWeight = FontWeight.SemiBold)
                    candidate.latestAnalysis?.let { analysis ->
                        analysis.statusMetadataText()?.let { status ->
                            Text(
                                status,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        analysis.dataBasisText()?.let { dataBasis ->
                            Text(
                                dataBasis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

internal const val RECENT_ANALYSIS_LIMIT = 5
