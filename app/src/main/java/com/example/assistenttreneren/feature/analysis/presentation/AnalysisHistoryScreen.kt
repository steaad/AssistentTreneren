package com.example.assistenttreneren.feature.analysis.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHistoryScreen(
    onNavigateBack: () -> Unit,
    onShowAnalysis: (String) -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val olderAnalyses = uiState.completedAnalyses.drop(RECENT_ANALYSIS_LIMIT)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historikk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
        ) {
            Text("Tidligere analyser", style = MaterialTheme.typography.titleMedium)
            CompletedAnalysisList(
                analyses = olderAnalyses,
                onAnalysisSelected = onShowAnalysis,
                emptyMessage = if (uiState.isLoadingCandidates) {
                    "Henter analyser …"
                } else {
                    "Ingen eldre analyser å vise."
                },
            )
        }
    }
}
