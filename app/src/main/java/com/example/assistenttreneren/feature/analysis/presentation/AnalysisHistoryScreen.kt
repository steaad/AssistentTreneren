package com.example.assistenttreneren.feature.analysis.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHistoryScreen(
    onNavigateBack: () -> Unit,
    onShowAnalysis: (String) -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historikk") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Tilbake") } },
            )
        },
    ) { paddingValues ->
        if (uiState.completedAnalyses.isEmpty()) {
            Text(if (uiState.isLoadingCandidates) "Henter analyser …" else "Ingen analyser å vise.", Modifier.padding(paddingValues).padding(20.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp, paddingValues.calculateTopPadding() + 16.dp, 20.dp, paddingValues.calculateBottomPadding() + 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.completedAnalyses, key = { it.activityId }) { candidate ->
                    ActivityAnalysisHistoryGroup(candidate, uiState.activityAnalyses[candidate.activityId], candidate.activityId in uiState.loadingActivityAnalysisIds, { viewModel.loadActivityAnalyses(candidate.activityId) }, onShowAnalysis)
                }
            }
        }
    }
}

@Composable
private fun ActivityAnalysisHistoryGroup(candidate: AnalysisCandidate, analyses: List<AnalysisSummary>?, isLoading: Boolean, onExpand: () -> Unit, onShowAnalysis: (String) -> Unit) {
    var expanded by rememberSaveable(candidate.activityId) { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded; if (expanded) onExpand() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(candidate.title ?: "Aktivitet uten tittel", fontWeight = FontWeight.SemiBold)
                    Text("Analyse v${candidate.latestAnalysis?.version ?: 0} er nyeste versjon", style = MaterialTheme.typography.bodySmall)
                }
                Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (expanded) "Skjul analyser" else "Vis analyser")
            }
            if (expanded) Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    analyses.isNullOrEmpty() -> Text("Ingen analyser for denne aktiviteten.")
                    else -> analyses.forEach { analysis ->
                        Text(
                            "Analyse v${analysis.version} · ${analysis.createdAt.toNorwegianDate()} · ${if (analysis.version == 1) "Første analyse" else "Oppdatert analyse"}",
                            Modifier.fillMaxWidth().clickable { onShowAnalysis(analysis.analysisId) }.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

private fun String.toNorwegianDate(): String = runCatching {
    Instant.parse(this).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d. MMMM", Locale("nb", "NO")))
}.getOrDefault(this)
