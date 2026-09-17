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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisEvidence
import com.example.assistenttreneren.feature.analysis.domain.model.MatchAnalysisResult
import com.example.assistenttreneren.feature.analysis.domain.model.MatchPhase
import com.example.assistenttreneren.feature.analysis.domain.model.TrainingAnalysisResult
import com.example.assistenttreneren.feature.analysis.domain.model.TrainingSection
import com.example.assistenttreneren.feature.analysis.domain.model.CoachIntervention

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalysisResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.trainingResult != null || uiState.analysisMetadata?.activityCategory == "Trening") "Treningsanalyse" else "Kampanalyse") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },
            )
        },
    ) { paddingValues ->
        val matchResult = uiState.matchResult
        when {
            uiState.isLoading -> LoadingResult(modifier = Modifier.padding(paddingValues))
            matchResult != null -> MatchAnalysisContent(
                result = matchResult,
                analysisMetadata = uiState.analysisMetadata,
                contentPadding = paddingValues,
            )

            uiState.trainingResult != null -> TrainingAnalysisContent(
                result = requireNotNull(uiState.trainingResult),
                analysisMetadata = uiState.analysisMetadata,
                contentPadding = paddingValues,
            )

            uiState.unsupportedResult != null -> UnsupportedResult(
                rawResult = uiState.unsupportedResult.toString(),
                modifier = Modifier.padding(paddingValues),
            )

            else -> ResultError(
                message = uiState.errorMessage ?: "Kunne ikke vise analysen.",
                onRetry = viewModel::retry,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun TrainingAnalysisContent(
    result: TrainingAnalysisResult,
    analysisMetadata: com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob?,
    contentPadding: PaddingValues,
) {
    val training = result.training
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        analysisMetadata?.let { metadata -> item { AnalysisMetadataCard(metadata) } }
        item {
            AnalysisSection("Kort oppsummering", "training-summary", expandedInitially = true) {
                Text(result.executiveSummary.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(result.executiveSummary.summary)
                Text(training.summary, style = MaterialTheme.typography.bodyMedium)
                InsightTextList("Viktigste punkter", result.executiveSummary.keyTakeaways)
                InsightTextList("Positive utviklingstrekk", result.executiveSummary.positiveDevelopments)
                InsightTextList("Viktige bekymringer", result.executiveSummary.mainConcerns)
            }
        }
        item {
            AnalysisSection("Læringsmål og læring", "training-learning") {
                InsightTextList("Valgte læringsmål", result.learningSummary.statedLearningObjectives)
                InsightTextList("Hva spillerne ser ut til å forstå", result.learningSummary.whatPlayersAppearToUnderstand)
                InsightTextList("Hva de trenger flere erfaringer med", result.learningSummary.whatPlayersStillStruggleWith)
                InsightTextList("Neste læringsprioriteter", result.learningSummary.nextLearningPriorities)
            }
        }
        item { TrainingSectionGroup("Øvelsesdel", training.exercise, "Ingen lydopptak i Øvelse-delen.") }
        item { TrainingSectionGroup("Spilldel", training.game, "Ingen lydopptak i Spill-delen.") }
        item { TrainingSectionGroup("Evaluering", training.evaluation, "Ingen lydopptak i Evalueringsdelen.") }
        item {
            AnalysisSection("Læringsprogresjon", "training-progression") {
                when {
                    training.exercise != null && training.game != null -> {
                        Text("Fra øvelse til spill", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        EvidenceGroup("Progresjon", training.learningProgressions)
                    }
                    training.learningProgressions.isNotEmpty() -> {
                        Text("Overføringen fra øvelse til spill kunne ikke vurderes fullt ut fordi en av delene mangler.")
                        EvidenceGroup("Progresjon", training.learningProgressions)
                    }
                    else -> Text("Ingen læringsprogresjon kunne identifiseres i datagrunnlaget.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            AnalysisSection("Lag- og spillerutvikling", "training-development") {
                Text(result.teamDevelopmentSummary.overallSummary)
                EvidenceGroup("Lagets utvikling", result.teamDevelopmentSummary.players)
                Text(result.playerDevelopmentSummary.overallSummary)
                EvidenceGroup("Spillerutvikling", result.playerDevelopmentSummary.players)
                EvidenceGroup("Spilleroppfølging", training.playerFollowUp)
            }
        }
        item {
            AnalysisSection("Trenerinteraksjoner (${result.coachInterventions.size})", "training-coach-interactions") {
                CoachInterventions(result.coachInterventions)
            }
        }
        item {
            AnalysisSection("Mønstre og prioriteringer", "training-patterns") {
                EvidenceGroup("Mønstre", result.patterns)
                EvidenceGroup("Prioriteringer", result.priorities)
            }
        }
        item { AnalysisSection("Anbefalinger", "training-recommendations") { EvidenceGroup("Anbefalinger", result.recommendations) } }
        item { AnalysisSection("Usikkerhet i datagrunnlaget", "training-uncertainties") { EvidenceGroup("Usikkerheter", result.uncertainties) } }
    }
}

@Composable
private fun TrainingSectionGroup(title: String, section: TrainingSection?, emptyMessage: String) {
    AnalysisSection(title, "training-${title.lowercase()}") {
        if (section == null) {
            Text(emptyMessage, style = MaterialTheme.typography.bodySmall)
        } else {
            Text(section.summary)
            EvidenceGroup("Funn", section.findings)
            EvidenceGroup("Usikkerheter", section.uncertainties)
        }
    }
}

@Composable
private fun LoadingResult(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text("Henter analyse …", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ResultError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Prøv igjen")
        }
    }
}

@Composable
private fun UnsupportedResult(
    rawResult: String,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Denne analyseformen har ikke en tilpasset visning ennå.")
        }
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Text(rawResult, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MatchAnalysisContent(
    result: MatchAnalysisResult,
    analysisMetadata: com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob?,
    contentPadding: PaddingValues,
) {
    val actionCount = result.priorities.size + result.recommendations.size + result.match.summary.recommendedFollowUp.size
    val playerCount = result.playerDevelopmentSummary.teamDevelopmentPriorities.size +
        result.playerDevelopmentSummary.players.size + result.match.playerFollowUp.size
    val phaseCount = result.match.firstHalf.size + result.match.halftime.size +
        result.match.secondHalf.size + result.match.evaluation.size

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        analysisMetadata?.let { metadata ->
            item { AnalysisMetadataCard(metadata) }
        }
        item {
            AnalysisSection(title = "Kampbildet", expandedInitially = true, sectionKey = "match-overview") {
                Text(result.executiveSummary.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(result.executiveSummary.summary)
                Text(result.match.summary.overallMatchAssessment, style = MaterialTheme.typography.bodyMedium)
                InsightTextList("Nøkkelpunkter", result.executiveSummary.keyTakeaways)
                InsightTextList("Kampfortelling", listOf(result.match.summary.matchStory))
                InsightTextList("Styrker", result.match.summary.mainStrengths)
                InsightTextList("Positive utviklingstrekk", result.executiveSummary.positiveDevelopments)
                InsightTextList("Utviklingsområder", result.match.summary.mainDevelopmentAreas)
                InsightTextList("Hovedbekymringer", result.executiveSummary.mainConcerns)
            }
        }
        item {
            AnalysisSection("Prioriteringer og anbefalinger ($actionCount)", sectionKey = "actions") {
                EvidenceGroup("Prioriteringer", result.priorities)
                EvidenceGroup("Anbefalinger", result.recommendations)
                EvidenceGroup("Anbefalt oppfølging", result.match.summary.recommendedFollowUp)
            }
        }
        item {
            AnalysisSection("Mønstre (${result.patterns.size})", sectionKey = "patterns") {
                EvidenceGroup("Mønstre", result.patterns)
            }
        }
        item {
            AnalysisSection("Spillerutvikling ($playerCount)", sectionKey = "players") {
                Text(result.playerDevelopmentSummary.overallSummary)
                EvidenceGroup("Lagets utviklingsprioriteringer", result.playerDevelopmentSummary.teamDevelopmentPriorities)
                EvidenceGroup("Spillerpunkter", result.playerDevelopmentSummary.players)
                EvidenceGroup("Spilleroppfølging", result.match.playerFollowUp)
            }
        }
        item {
            AnalysisSection("Trenerinteraksjoner (${result.coachInterventions.size})", sectionKey = "match-coach-interactions") {
                CoachInterventions(result.coachInterventions)
            }
        }
        item {
            AnalysisSection("Kampfaser ($phaseCount)", sectionKey = "phases") {
                EvidenceGroup("Sammenligning mellom faser", result.match.phaseComparisons)
                PhaseGroup("1. omgang", result.match.firstHalf)
                PhaseGroup("Pause", result.match.halftime)
                PhaseGroup("2. omgang", result.match.secondHalf)
                PhaseGroup("Evaluering", result.match.evaluation)
            }
        }
        item {
            AnalysisSection("Usikkerhet og grunnlag (${result.uncertainties.size})", sectionKey = "uncertainties") {
                EvidenceGroup("Usikkerheter", result.uncertainties)
            }
        }
    }
}

@Composable
private fun AnalysisSection(
    title: String,
    sectionKey: String,
    expandedInitially: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(sectionKey) { mutableStateOf(expandedInitially) }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Skjul $title" else "Vis $title",
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun InsightTextList(title: String, insights: List<String>) {
    if (insights.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        insights.forEach { insight -> Text("• $insight") }
    }
}

@Composable
private fun EvidenceGroup(title: String, evidence: List<AnalysisEvidence>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (evidence.isEmpty()) {
            Text("Ingen funn i denne delen.", style = MaterialTheme.typography.bodySmall)
        } else {
            evidence.forEach { EvidenceCard(it) }
        }
    }
}

@Composable
private fun EvidenceCard(evidence: AnalysisEvidence) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(evidence.title, fontWeight = FontWeight.SemiBold)
            Text(evidence.description)
            if (evidence.relatedPlayerNames.isNotEmpty()) {
                Text(
                    text = evidence.relatedPlayerNames.joinToString(separator = " · "),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            val sourceText = evidence.sourceText()
            if (sourceText != null) {
                Text(sourceText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CoachInterventions(interventions: List<CoachIntervention>) {
    if (interventions.isEmpty()) {
        Text("Ingen sikre trenerinteraksjoner ble identifisert.", style = MaterialTheme.typography.bodySmall)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        interventions.forEach { intervention ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "${intervention.type.toDisplayName()} · ${intervention.section}",
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(intervention.summary)
                    Text(
                        text = "Høy sikkerhet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    intervention.sourceText()?.let { sourceText ->
                        Text(sourceText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhaseGroup(title: String, phases: List<MatchPhase>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (phases.isEmpty()) {
            Text("Ingen analyse for denne fasen.", style = MaterialTheme.typography.bodySmall)
        } else {
            phases.forEach { phase ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(phase.subcategory, fontWeight = FontWeight.SemiBold)
                        Text(phase.summary)
                        EvidenceGroup("Funn", phase.findings)
                        EvidenceGroup("Usikkerheter", phase.uncertainties)
                    }
                }
            }
        }
    }
}

private fun AnalysisEvidence.sourceText(): String? {
    val sources = buildList {
        if (relatedEventIds.isNotEmpty()) add("${relatedEventIds.size} ${if (relatedEventIds.size == 1) "hendelse" else "hendelser"}")
        if (relatedTranscriptionIds.isNotEmpty()) add("${relatedTranscriptionIds.size} ${if (relatedTranscriptionIds.size == 1) "transkripsjon" else "transkripsjoner"}")
    }
    return sources.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun CoachIntervention.sourceText(): String? {
    val sources = buildList {
        if (relatedEventIds.isNotEmpty()) add("${relatedEventIds.size} ${if (relatedEventIds.size == 1) "hendelse" else "hendelser"}")
        if (relatedTranscriptionIds.isNotEmpty()) add("${relatedTranscriptionIds.size} ${if (relatedTranscriptionIds.size == 1) "transkripsjon" else "transkripsjoner"}")
    }
    return sources.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun String.toDisplayName(): String =
    when (this) {
        "INSTRUCTION" -> "Instruksjon"
        "QUESTION" -> "Spørsmål"
        "FEEDBACK" -> "Tilbakemelding"
        "REINFORCEMENT" -> "Forsterkning"
        "ORGANISATION" -> "Organisering"
        else -> this
    }
