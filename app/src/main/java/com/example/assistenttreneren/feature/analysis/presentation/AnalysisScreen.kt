package com.example.assistenttreneren.feature.analysis.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidate
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showActivityPicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Analyse") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::refreshCandidates,
                        enabled = !uiState.isLoadingCandidates,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Oppdater aktiviteter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        AnalysisContent(
            uiState = uiState,
            onChooseActivity = { showActivityPicker = true },
            onGenerateAnalysis = viewModel::startSelectedAnalysis,
            onShowAnalysis = viewModel::loadCompletedAnalysis,
            modifier = Modifier.padding(paddingValues),
        )
    }

    if (showActivityPicker) {
        ActivityPickerDialog(
            candidates = uiState.candidates,
            onSelected = {
                viewModel.onActivitySelected(it)
                showActivityPicker = false
            },
            onDismiss = { showActivityPicker = false },
        )
    }
}

@Composable
private fun AnalysisContent(
    uiState: AnalysisUiState,
    onChooseActivity: () -> Unit,
    onGenerateAnalysis: () -> Unit,
    onShowAnalysis: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        Text("Velg aktivitet", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = onChooseActivity,
            enabled = uiState.candidates.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = uiState.selectedCandidate?.title ?: "Velg en aktivitet",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (uiState.isLoadingCandidates) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
                Text("Henter analyseaktiviteter …")
            }
        }

        val selectedCandidate = uiState.selectedCandidate
        if (selectedCandidate != null) {
            val candidate = selectedCandidate
            CandidateStatusCard(candidate)
            AnalysisAction(
                uiState = uiState,
                candidate = candidate,
                onGenerateAnalysis = onGenerateAnalysis,
                onShowAnalysis = onShowAnalysis,
            )
        } else if (!uiState.isLoadingCandidates) {
            Text("Ingen aktiviteter er tilgjengelige for analyse.")
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        uiState.completedAnalysis?.let { analysis ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Analyse klar", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (analysis.result == null) {
                            "Analysen er fullført. Resultatvisningen bygges i neste steg."
                        } else {
                            "Analysen er fullført og klar for resultatvisning."
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateStatusCard(candidate: AnalysisCandidate) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(candidate.state.toDisplayText(), fontWeight = FontWeight.SemiBold)
            Text(candidate.message)
        }
    }
}

@Composable
private fun AnalysisAction(
    uiState: AnalysisUiState,
    candidate: AnalysisCandidate,
    onGenerateAnalysis: () -> Unit,
    onShowAnalysis: () -> Unit,
) {
    when {
        uiState.isAnalysisInProgress || uiState.isStartingAnalysis -> {
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Analyse pågår …")
            }
        }

        candidate.state == AnalysisCandidateState.COMPLETED -> {
            Button(onClick = onShowAnalysis, modifier = Modifier.fillMaxWidth()) {
                Text("Se analyse")
            }
        }

        candidate.state == AnalysisCandidateState.READY -> {
            Button(onClick = onGenerateAnalysis, modifier = Modifier.fillMaxWidth()) {
                Text("Generer analyse")
            }
        }

        else -> {
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Generer analyse")
            }
        }
    }
}

@Composable
private fun ActivityPickerDialog(
    candidates: List<AnalysisCandidate>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Velg aktivitet") },
        text = {
            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                items(candidates, key = { it.activityId }) { candidate ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(candidate.activityId) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(candidate.title ?: "Uten tittel", fontWeight = FontWeight.Medium)
                        Text(candidate.state.toDisplayText(), style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("Lukk") }
        },
    )
}

private fun AnalysisCandidateState.toDisplayText(): String =
    when (this) {
        AnalysisCandidateState.READY -> "Klar for analyse"
        AnalysisCandidateState.TRANSCRIPTION_PENDING -> "Transkripsjon pågår"
        AnalysisCandidateState.REVIEW_REQUIRED -> "Trenger gjennomgang"
        AnalysisCandidateState.INPUT_INVALID -> "Ugyldig grunnlag"
        AnalysisCandidateState.PROCESSING -> "Analyse pågår"
        AnalysisCandidateState.COMPLETED -> "Analyse fullført"
    }
