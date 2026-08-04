package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
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
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus
import java.util.Locale
import kotlin.math.roundToLong

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
        ExpandableSection("Opptak", false, icon = Icons.Outlined.Mic) { state.recordings.forEach { Text("${it.displayName} · ${it.subCategory}", Modifier.padding(vertical = 4.dp)) } }
        ExpandableSection("Opplasting", false, icon = Icons.Outlined.CloudUpload) { state.uploadJobs.forEach { uploadJob -> UploadSummaryItem(uploadJob.recordingDisplayName, uploadJob.status, uploadJob.statusMessage) } }
        TranscriptionSection(state, viewModel)
        Text(stringResource(R.string.summary_finish_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean,
    stateKey: String = title,
    icon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(stateKey) { mutableStateOf(initiallyExpanded) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                icon?.let { Icon(it, contentDescription = null) }
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            IconButton({ expanded = !expanded }) { Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null) }
        }
        if (expanded) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    } }
}

@Composable
private fun TranscriptionSection(
    state: SummaryStepUiState,
    viewModel: SummaryStepViewModel,
) {
    ExpandableSection("Transkripsjon", true, icon = Icons.Outlined.TextSnippet) {
        when {
            state.isLoadingTranscriptionReview -> Text("Henter transkripsjoner...")
            state.transcriptionErrorMessage != null -> { Text(state.transcriptionErrorMessage, color = MaterialTheme.colorScheme.error); OutlinedButton(viewModel::refreshTranscriptionReview) { Text("Prøv igjen") } }
            state.transcriptionReview == null || state.transcriptionReview.recordings.isEmpty() -> Text("Ingen ferdige transkripsjoner ennå.")
            else -> state.transcriptionReview.recordings.forEach { recording ->
                OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${recording.category} - ${recording.subCategory}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    ExpandableSection("Tekst", false, "${recording.recordingId}-text", Icons.Outlined.Article) {
                        if (recording.transcriptText.isBlank()) {
                            Text("Ingen transkribert tekst.")
                        } else {
                            Text(recording.transcriptText)
                        }
                    }
                    ExpandableSection("Observasjoner (${recording.events.size})", false, "${recording.recordingId}-events", Icons.Outlined.Visibility) {
                        if (recording.events.isEmpty()) Text("Ingen observasjoner.")
                        recording.events.forEach { event -> EventItem(event, state.isSubmittingTranscriptionAction, viewModel) }
                    }
                    ExpandableSection("Trenger gjennomgang (${recording.issues.size})", recording.issues.isNotEmpty(), "${recording.recordingId}-issues", Icons.Outlined.WarningAmber) {
                        if (recording.issues.isEmpty()) Text("Ingen avvik trenger gjennomgang.")
                        recording.issues.forEach { issue -> IssueItem(issue, state.isSubmittingTranscriptionAction, viewModel) }
                    }
                } }
            }
        }
    }
}

@Composable
private fun EventItem(event: TranscriptionEvent, busy: Boolean, viewModel: SummaryStepViewModel) {
    var edit by rememberSaveable(event.eventId) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable("${event.eventId}-delete") { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) {
        Text(event.text); Text("${formatSeconds(event.startMillis)}–${formatSeconds(event.endMillis)}${if (event.manuallyEdited) " · redigert" else ""}", style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { edit = true }, enabled = !busy) {
                Icon(Icons.Outlined.Edit, contentDescription = "Rediger")
            }
            IconButton(onClick = { confirmDelete = true }, enabled = !busy) {
                Icon(Icons.Outlined.Delete, contentDescription = "Slett")
            }
        }
    } }
    if (edit) EventInputDialog("Rediger observasjon", event.text, event.startMillis, event.endMillis, busy, { edit = false }) { viewModel.updateEvent(event.eventId, it); edit = false }
    if (confirmDelete) DeleteConfirmationDialog(busy, { confirmDelete = false }) { viewModel.deleteEvent(event.eventId); confirmDelete = false }
}

@Composable
private fun DeleteConfirmationDialog(busy: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Slett observasjon?") },
        text = { Text("Er du helt sikker på at du vil slette denne observasjonen? Handlingen kan ikke angres.") },
        confirmButton = { Button(onClick = onConfirm, enabled = !busy) { Text("Slett") } },
        dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !busy) { Text("Avbryt") } },
    )
}

@Composable
private fun UploadSummaryItem(recordingName: String, status: UploadStatus, statusMessage: String?) {
    val statusColor = when (status) {
        UploadStatus.Completed -> MaterialTheme.colorScheme.primary
        UploadStatus.Failed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(modifier = Modifier.padding(top = 5.dp), shape = CircleShape, color = statusColor) { Text("", modifier = Modifier.padding(6.dp)) }
        Column { Text(recordingName, style = MaterialTheme.typography.titleSmall); Text(statusMessage ?: status.name, style = MaterialTheme.typography.bodySmall, color = statusColor) }
    }
}

@Composable
private fun IssueItem(issue: TranscriptionEventIssue, busy: Boolean, viewModel: SummaryStepViewModel) {
    var resolve by rememberSaveable(issue.issueId) { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) {
        Text(issue.issueType, style = MaterialTheme.typography.titleSmall); issue.candidateText?.let { Text(it) }; issue.contextBefore?.let { Text("Før: $it", style = MaterialTheme.typography.bodySmall) }; issue.contextAfter?.let { Text("Etter: $it", style = MaterialTheme.typography.bodySmall) }
        if (issue.startMillis != null && issue.endMillis != null) Text("${formatSeconds(issue.startMillis)}–${formatSeconds(issue.endMillis)}", style = MaterialTheme.typography.bodySmall)
        Row { OutlinedButton({ resolve = true }, enabled = !busy) { Text("Lagre korrigert observasjon") }; OutlinedButton({ viewModel.dismissIssue(issue.issueId) }, enabled = !busy) { Text("Avvis") } }
    } }
    if (resolve) EventInputDialog("Løs avvik", issue.candidateText.orEmpty(), issue.startMillis ?: 0, issue.endMillis ?: 0, busy, { resolve = false }) { viewModel.resolveIssue(issue.issueId, it); resolve = false }
}

@Composable
private fun EventInputDialog(title: String, initialText: String, initialStart: Long, initialEnd: Long, busy: Boolean, onDismiss: () -> Unit, onConfirm: (TranscriptionEventInput) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initialText) }; var start by rememberSaveable { mutableStateOf(formatSecondsValue(initialStart)) }; var end by rememberSaveable { mutableStateOf(formatSecondsValue(initialEnd)) }
    val startMillis = secondsToMillis(start)
    val endMillis = secondsToMillis(end)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(text, { text = it }, label = { Text("Tekst") }); OutlinedTextField(start, { start = it }, label = { Text("Start (sekunder)") }); OutlinedTextField(end, { end = it }, label = { Text("Slutt (sekunder)") }) } }, confirmButton = { Button(onClick = { if (startMillis != null && endMillis != null) onConfirm(TranscriptionEventInput(text, startMillis, endMillis)) }, enabled = !busy && text.isNotBlank() && startMillis != null && endMillis != null) { Text("Lagre") } }, dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !busy) { Text("Avbryt") } })
}

private fun formatSeconds(millis: Long): String = "${formatSecondsValue(millis)} s"

private fun formatSecondsValue(millis: Long): String =
    if (millis % 1_000L == 0L) {
        (millis / 1_000L).toString()
    } else {
        String.format(Locale.getDefault(), "%.3f", millis / 1_000.0)
            .trimEnd('0')
            .trimEnd(',', '.')
    }

private fun secondsToMillis(seconds: String): Long? =
    seconds.trim()
        .replace(',', '.')
        .toDoubleOrNull()
        ?.takeIf { it >= 0 }
        ?.let { (it * 1_000).roundToLong() }
