package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.SummaryStepUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.SummaryStepViewModel
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEvent
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventIssue

@Composable
fun SummaryStepScreen(uiState: CoachActivityWizardUiState, onStepOpened: (CoachActivityWizardStep) -> Unit, onNavigateBack: () -> Unit, onFinish: () -> Unit, summaryStepViewModel: SummaryStepViewModel = hiltViewModel()) {
    val summary by summaryStepViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { onStepOpened(CoachActivityWizardStep.Summary) }
    LaunchedEffect(uiState.selectedActivityId) { summaryStepViewModel.loadActivity(uiState.selectedActivityId) }
    CoachActivityWizardScaffold(title = stringResource(R.string.wizard_summary_title), uiState = uiState, onNavigateBack = onNavigateBack, onNavigateNext = onFinish, nextButtonText = stringResource(R.string.wizard_finish_button)) {
        SummaryContent(uiState, summary, summaryStepViewModel)
    }
}

@Composable
private fun SummaryContent(wizard: CoachActivityWizardUiState, state: SummaryStepUiState, viewModel: SummaryStepViewModel) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(wizard.title.ifBlank { stringResource(R.string.summary_missing_title) }, style = MaterialTheme.typography.titleMedium)
            Text(wizard.activityCategory ?: stringResource(R.string.summary_missing_category))
            Text("${state.recordings.size} opptak: ${state.audioRecordingCount} lyd, ${state.videoRecordingCount} video")
            Text("${state.completedUploadCount} fullført, ${state.failedUploadCount} feilet")
            Text("Transkripsjon: ${state.processingAudioCount} under behandling, ${state.readyTranscriptionCount} klare, ${state.failedAudioCount} feilet, ${state.pendingIssueCount} trenger gjennomgang")
        } }
        ExpandableSection("Opptak", false) { state.recordings.forEach { Text("${it.displayName} · ${it.subCategory}", Modifier.padding(vertical = 4.dp)) } }
        ExpandableSection("Opplasting", false) { state.uploadJobs.forEach { Text("${it.recordingDisplayName}: ${it.statusMessage ?: it.status.name}", Modifier.padding(vertical = 4.dp)) } }
        TranscriptionSection(state, viewModel)
        Text(stringResource(R.string.summary_finish_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExpandableSection(title: String, initiallyExpanded: Boolean, content: @Composable () -> Unit) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            IconButton({ expanded = !expanded }) { Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null) }
        }
        if (expanded) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    } }
}

@Composable
private fun TranscriptionSection(state: SummaryStepUiState, viewModel: SummaryStepViewModel) {
    ExpandableSection("Transkripsjon", true) {
        when {
            state.isLoadingTranscriptionReview -> Text("Henter transkripsjoner...")
            state.transcriptionErrorMessage != null -> { Text(state.transcriptionErrorMessage, color = MaterialTheme.colorScheme.error); OutlinedButton(viewModel::refreshTranscriptionReview) { Text("Prøv igjen") } }
            state.transcriptionReview == null || state.transcriptionReview.recordings.isEmpty() -> Text("Ingen ferdige transkripsjoner ennå.")
            else -> state.transcriptionReview.recordings.forEach { recording ->
                OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(recording.recordingId, style = MaterialTheme.typography.titleSmall)
                    Text(recording.transcriptText, style = MaterialTheme.typography.bodySmall)
                    Text("Observasjoner", style = MaterialTheme.typography.titleSmall)
                    recording.events.forEach { event -> EventItem(event, state.isSubmittingTranscriptionAction, viewModel) }
                    Text("Trenger gjennomgang (${recording.issues.size})", style = MaterialTheme.typography.titleSmall)
                    recording.issues.forEach { issue -> IssueItem(issue, state.isSubmittingTranscriptionAction, viewModel) }
                } }
            }
        }
    }
}

@Composable
private fun EventItem(event: TranscriptionEvent, busy: Boolean, viewModel: SummaryStepViewModel) {
    var edit by rememberSaveable(event.eventId) { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) {
        Text(event.text); Text("${event.startMillis}–${event.endMillis} ms${if (event.manuallyEdited) " · redigert" else ""}", style = MaterialTheme.typography.bodySmall)
        Row { OutlinedButton({ edit = true }, enabled = !busy) { Text("Rediger") }; OutlinedButton({ viewModel.deleteEvent(event.eventId) }, enabled = !busy) { Text("Slett") } }
    } }
    if (edit) EventInputDialog("Rediger observasjon", event.text, event.startMillis, event.endMillis, busy, { edit = false }) { viewModel.updateEvent(event.eventId, it); edit = false }
}

@Composable
private fun IssueItem(issue: TranscriptionEventIssue, busy: Boolean, viewModel: SummaryStepViewModel) {
    var resolve by rememberSaveable(issue.issueId) { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) {
        Text(issue.issueType, style = MaterialTheme.typography.titleSmall); issue.candidateText?.let { Text(it) }; issue.contextBefore?.let { Text("Før: $it", style = MaterialTheme.typography.bodySmall) }; issue.contextAfter?.let { Text("Etter: $it", style = MaterialTheme.typography.bodySmall) }
        Row { OutlinedButton({ resolve = true }, enabled = !busy) { Text("Lagre korrigert observasjon") }; OutlinedButton({ viewModel.dismissIssue(issue.issueId) }, enabled = !busy) { Text("Avvis") } }
    } }
    if (resolve) EventInputDialog("Løs avvik", issue.candidateText.orEmpty(), issue.startMillis ?: 0, issue.endMillis ?: 0, busy, { resolve = false }) { viewModel.resolveIssue(issue.issueId, it); resolve = false }
}

@Composable
private fun EventInputDialog(title: String, initialText: String, initialStart: Long, initialEnd: Long, busy: Boolean, onDismiss: () -> Unit, onConfirm: (TranscriptionEventInput) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initialText) }; var start by rememberSaveable { mutableStateOf(initialStart.toString()) }; var end by rememberSaveable { mutableStateOf(initialEnd.toString()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(text, { text = it }, label = { Text("Tekst") }); OutlinedTextField(start, { start = it }, label = { Text("Start (ms)") }); OutlinedTextField(end, { end = it }, label = { Text("Slutt (ms)") }) } }, confirmButton = { Button(onClick = { val s = start.toLongOrNull(); val e = end.toLongOrNull(); if (s != null && e != null) onConfirm(TranscriptionEventInput(text, s, e)) }, enabled = !busy && text.isNotBlank() && start.toLongOrNull() != null && end.toLongOrNull() != null) { Text("Lagre") } }, dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !busy) { Text("Avbryt") } })
}
