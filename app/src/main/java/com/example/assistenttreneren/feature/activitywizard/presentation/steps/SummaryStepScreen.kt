package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.SwapHoriz
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
import com.example.assistenttreneren.feature.activitywizard.presentation.eventsFor
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEvent
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventIssue
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventType
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

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
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var savedExpanded by rememberSaveable(stateKey) { mutableStateOf(initiallyExpanded) }
    val currentExpanded = expanded ?: savedExpanded
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                icon?.let { Icon(it, contentDescription = null) }
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(
                onClick = {
                    val newExpanded = !currentExpanded
                    if (onExpandedChange == null) savedExpanded = newExpanded else onExpandedChange(newExpanded)
                },
            ) { Icon(if (currentExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null) }
        }
        if (currentExpanded) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    } }
}

@Composable
private fun TranscriptionSection(
    state: SummaryStepUiState,
    viewModel: SummaryStepViewModel,
) {
    ExpandableSection(
        title = "Transkripsjon (${state.transcriptionReview?.recordings?.size ?: 0})",
        initiallyExpanded = false,
        icon = Icons.AutoMirrored.Outlined.TextSnippet,
    ) {
        when {
            state.isLoadingTranscriptionReview -> Text("Henter transkripsjoner...")
            state.transcriptionErrorMessage != null -> { Text(state.transcriptionErrorMessage, color = MaterialTheme.colorScheme.error); OutlinedButton(viewModel::refreshTranscriptionReview) { Text("Prøv igjen") } }
            state.transcriptionReview == null || state.transcriptionReview.recordings.isEmpty() -> Text("Ingen ferdige transkripsjoner ennå.")
            else -> state.transcriptionReview.recordings.forEach { recording ->
                var issueSectionExpanded by rememberSaveable("${recording.recordingId}-issues-expanded") {
                    mutableStateOf(false)
                }
                val matchClockRange = if (
                    recording.matchClockStartMillis != null && recording.matchClockEndMillis != null
                ) {
                    " - ${formatMatchClockTime(recording.matchClockStartMillis)}-${formatMatchClockTime(recording.matchClockEndMillis)}"
                } else {
                    ""
                }
                ExpandableSection(
                    title = "${recording.category} - ${recording.subCategory}$matchClockRange",
                    initiallyExpanded = false,
                    stateKey = "${recording.recordingId}-recording",
                ) {
                    val observations = recording.eventsFor(TranscriptionEventType.OBSERVATION)
                    val statistics = recording.eventsFor(TranscriptionEventType.STAT)
                    val substitutions = recording.eventsFor(TranscriptionEventType.SUBSTITUTION)
                    val startingLineups = recording.eventsFor(TranscriptionEventType.STARTING_LINEUP)
                    ExpandableSection("Tekst", false, "${recording.recordingId}-text", Icons.AutoMirrored.Outlined.Article) {
                        if (recording.transcriptText.isBlank()) {
                            Text("Ingen transkribert tekst.")
                        } else {
                            Text(recording.transcriptText)
                        }
                    }
                    ExpandableSection("Observasjoner (${observations.size})", false, "${recording.recordingId}-events", Icons.Outlined.Visibility) {
                        if (observations.isEmpty()) Text("Ingen observasjoner.")
                        observations.forEach { event -> EventItem(event, state.isSubmittingTranscriptionAction, viewModel) }
                    }
                    ExpandableSection("Statistikk (${statistics.size})", false, "${recording.recordingId}-statistics", Icons.Outlined.BarChart) {
                        if (statistics.isEmpty()) Text("Ingen statistikk.")
                        statistics.forEach { statistic -> EventItem(statistic, state.isSubmittingTranscriptionAction, viewModel) }
                    }
                    ExpandableSection("Spillerbytter (${substitutions.size})", false, "${recording.recordingId}-substitutions", Icons.Outlined.SwapHoriz) {
                        if (substitutions.isEmpty()) Text("Ingen spillerbytter.")
                        substitutions.forEach { substitution -> EventItem(substitution, state.isSubmittingTranscriptionAction, viewModel) }
                    }
                    ExpandableSection("Startoppstilling (${startingLineups.size})", false, "${recording.recordingId}-starting-lineups", Icons.Outlined.People) {
                        if (startingLineups.isEmpty()) Text("Ingen startoppstilling.")
                        startingLineups.forEach { lineup -> EventItem(lineup, state.isSubmittingTranscriptionAction, viewModel) }
                    }
                    ExpandableSection(
                        title = "Trenger gjennomgang (${recording.issues.size})",
                        initiallyExpanded = false,
                        stateKey = "${recording.recordingId}-issues",
                        icon = Icons.Outlined.WarningAmber,
                        expanded = issueSectionExpanded,
                        onExpandedChange = { issueSectionExpanded = it },
                    ) {
                        if (recording.issues.isEmpty()) Text("Ingen avvik trenger gjennomgang.")
                        recording.issues.forEach { issue ->
                            val relatedEvent = issue.relatedEventId?.let { relatedEventId ->
                                recording.events.firstOrNull { it.eventId == relatedEventId }
                            }
                            IssueItem(
                                issue = issue,
                                relatedEvent = relatedEvent,
                                busy = state.isSubmittingTranscriptionAction,
                                viewModel = viewModel,
                                onIssueHandled = {
                                    issueSectionExpanded = recording.issues.size > 1
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItem(event: TranscriptionEvent, busy: Boolean, viewModel: SummaryStepViewModel) {
    var edit by rememberSaveable(event.eventId) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable("${event.eventId}-delete") { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) {
        Text(
            when (event.eventType) {
                TranscriptionEventType.SUBSTITUTION -> "${event.playerOutName ?: "Ukjent"} ut → ${event.playerInName ?: "Ukjent"} inn"
                TranscriptionEventType.STARTING_LINEUP -> event.playerNames.joinToString(", ").ifBlank { event.text }
                else -> event.text
            },
        )
        Text(
            "${formatMatchClockTime(event.matchStartMillis ?: event.startMillis)}–" +
                "${formatMatchClockTime(event.matchEndMillis ?: event.endMillis)}" +
                if (event.manuallyEdited) " · redigert" else "",
            style = MaterialTheme.typography.bodySmall,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { edit = true }, enabled = !busy) {
                Icon(Icons.Outlined.Edit, contentDescription = "Rediger")
            }
            IconButton(onClick = { confirmDelete = true }, enabled = !busy) {
                Icon(Icons.Outlined.Delete, contentDescription = "Slett")
            }
        }
    } }
    if (edit) {
        EventInputDialog(
            title = when (event.eventType) {
                TranscriptionEventType.SUBSTITUTION -> "Rediger spillerbytte"
                TranscriptionEventType.STARTING_LINEUP -> "Rediger startoppstilling"
                else -> "Rediger event"
            },
            initialText = event.text,
            initialStart = event.matchStartMillis ?: event.startMillis,
            initialEnd = event.matchEndMillis ?: event.endMillis,
            initialEventType = event.eventType.takeIf {
                it in setOf(TranscriptionEventType.SUBSTITUTION, TranscriptionEventType.STARTING_LINEUP)
            },
            initialPlayerOutName = event.playerOutName,
            initialPlayerInName = event.playerInName,
            initialPlayerNames = event.playerNames,
            busy = busy,
            onDismiss = { edit = false },
        ) { input ->
            viewModel.updateEvent(event.eventId, input)
            edit = false
        }
    }
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
private fun IssueItem(
    issue: TranscriptionEventIssue,
    relatedEvent: TranscriptionEvent?,
    busy: Boolean,
    viewModel: SummaryStepViewModel,
    onIssueHandled: () -> Unit,
) {
    var resolve by rememberSaveable(issue.issueId) { mutableStateOf(false) }
    val requiredEventType = issue.requiredEventType()
    val relatedStartingLineup = relatedEvent?.takeIf {
        it.eventType == TranscriptionEventType.STARTING_LINEUP
    }
    val lockedEventType = requiredEventType ?: relatedStartingLineup?.eventType
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(issue.displayMessage(), style = MaterialTheme.typography.titleSmall)
            issue.excerpt?.takeIf { it.isNotBlank() }?.let { excerpt ->
                Text("Transkripsjonsutdrag", style = MaterialTheme.typography.labelLarge)
                Text(excerpt, style = MaterialTheme.typography.bodyMedium)
            }
            if (issue.startMillis != null && issue.endMillis != null) {
                Text(
                    "${formatMatchClockTime(issue.startMillis)}–${formatMatchClockTime(issue.endMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { resolve = true },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) { Text("Korriger") }
                OutlinedButton(
                    onClick = { viewModel.dismissIssue(issue.issueId, onIssueHandled) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) { Text("Avvis") }
            }
        }
    }
    if (resolve) EventInputDialog(
        title = "Løs avvik",
        initialText = relatedEvent?.text ?: issue.candidateText.orEmpty(),
        initialStart = issue.startMillis ?: 0,
        initialEnd = issue.endMillis ?: 0,
        initialEventType = relatedEvent?.eventType ?: requiredEventType,
        initialPlayerOutName = relatedEvent?.playerOutName,
        initialPlayerInName = relatedEvent?.playerInName,
        initialPlayerNames = relatedEvent?.playerNames.orEmpty(),
        allowEventTypeSelection = true,
        lockedEventType = lockedEventType,
        requiresStartingLineup = relatedStartingLineup != null ||
            requiredEventType == TranscriptionEventType.STARTING_LINEUP,
        busy = busy,
        onDismiss = { resolve = false },
    ) { input ->
        viewModel.resolveIssue(issue.issueId, input, onIssueHandled)
        resolve = false
    }
}

@Composable
private fun EventInputDialog(
    title: String,
    initialText: String,
    initialStart: Long,
    initialEnd: Long,
    initialEventType: TranscriptionEventType? = null,
    initialPlayerOutName: String? = null,
    initialPlayerInName: String? = null,
    initialPlayerNames: List<String> = emptyList(),
    busy: Boolean,
    onDismiss: () -> Unit,
    allowEventTypeSelection: Boolean = false,
    lockedEventType: TranscriptionEventType? = null,
    requiresStartingLineup: Boolean = false,
    onConfirm: (TranscriptionEventInput) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    var start by rememberSaveable { mutableStateOf(formatMatchClockInput(initialStart)) }
    var end by rememberSaveable { mutableStateOf(formatMatchClockInput(initialEnd)) }
    var eventType by rememberSaveable {
        mutableStateOf(lockedEventType ?: initialEventType ?: TranscriptionEventType.OBSERVATION)
    }
    var playerOutName by rememberSaveable { mutableStateOf(initialPlayerOutName.orEmpty()) }
    var playerInName by rememberSaveable { mutableStateOf(initialPlayerInName.orEmpty()) }
    var playerNamesText by rememberSaveable {
        mutableStateOf(initialPlayerNames.joinToString(", ").ifBlank { initialText })
    }
    val startMillis = matchClockToMillis(start)
    val endMillis = matchClockToMillis(end)
    val requiresPlayers = eventType == TranscriptionEventType.SUBSTITUTION &&
        (allowEventTypeSelection || initialEventType == TranscriptionEventType.SUBSTITUTION)
    val requiresPlayerList = eventType == TranscriptionEventType.STARTING_LINEUP &&
        (allowEventTypeSelection || initialEventType == TranscriptionEventType.STARTING_LINEUP)
    val playerNames = playerNamesText.toPlayerNames()
    val substitutionPreview = substitutionText(playerInName, playerOutName, text)
    val inputText = when {
        requiresPlayers -> substitutionPreview
        requiresPlayerList -> playerNames.joinToString(", ")
        else -> text
    }
    val inputIsValid = inputText.isNotBlank() &&
        startMillis != null &&
        endMillis != null &&
        (!requiresPlayers || (playerOutName.isNotBlank() && playerInName.isNotBlank())) &&
        (!requiresPlayerList || playerNames.isNotEmpty())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (allowEventTypeSelection) {
                    Text("Hendelsestype", style = MaterialTheme.typography.labelLarge)
                    if (lockedEventType != null || requiresStartingLineup) {
                        val type = lockedEventType ?: TranscriptionEventType.STARTING_LINEUP
                        EventTypeSelectionButton(
                            type = type,
                            label = type.displayName(),
                            selectedType = eventType,
                            onSelected = { eventType = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            EventTypeSelectionButton(
                                type = TranscriptionEventType.OBSERVATION,
                                label = "Observasjon",
                                selectedType = eventType,
                                onSelected = { eventType = it },
                                modifier = Modifier.weight(1f),
                            )
                            EventTypeSelectionButton(
                                type = TranscriptionEventType.STAT,
                                label = "Statistikk",
                                selectedType = eventType,
                                onSelected = { eventType = it },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        EventTypeSelectionButton(
                            type = TranscriptionEventType.SUBSTITUTION,
                            label = "Spillerbytte",
                            selectedType = eventType,
                            onSelected = { eventType = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (requiresPlayerList) {
                    OutlinedTextField(
                        value = playerNamesText,
                        onValueChange = { playerNamesText = it },
                        label = { Text("Spillere (kommaseparert)") },
                    )
                } else {
                    OutlinedTextField(
                        value = if (requiresPlayers) substitutionPreview else text,
                        onValueChange = { if (!requiresPlayers) text = it },
                        label = { Text("Tekst") },
                        readOnly = requiresPlayers,
                    )
                }
                if (requiresPlayers) {
                    OutlinedTextField(playerOutName, { playerOutName = it }, label = { Text("Spiller ut") })
                    OutlinedTextField(playerInName, { playerInName = it }, label = { Text("Spiller inn") })
                }
                OutlinedTextField(start, { start = it }, label = { Text("Start (kamptid)") })
                OutlinedTextField(end, { end = it }, label = { Text("Slutt (kamptid)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startMillis != null && endMillis != null) {
                        onConfirm(
                            TranscriptionEventInput(
                                text = inputText,
                                startMillis = startMillis,
                                endMillis = endMillis,
                                eventType = eventType.takeIf {
                                    allowEventTypeSelection ||
                                        initialEventType in setOf(
                                        TranscriptionEventType.SUBSTITUTION,
                                        TranscriptionEventType.STARTING_LINEUP,
                                    )
                                },
                                playerOutName = playerOutName.takeIf { requiresPlayers },
                                playerInName = playerInName.takeIf { requiresPlayers },
                                playerNames = playerNames.takeIf { requiresPlayerList },
                            ),
                        )
                    }
                },
                enabled = !busy && inputIsValid,
            ) { Text("Lagre") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !busy) { Text("Avbryt") } },
    )
}

@Composable
private fun EventTypeSelectionButton(
    type: TranscriptionEventType,
    label: String,
    selectedType: TranscriptionEventType,
    onSelected: (TranscriptionEventType) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedType == type) {
        Button(onClick = {}, enabled = false, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = { onSelected(type) }, modifier = modifier) { Text(label) }
    }
}

private fun formatMatchClockTime(millis: Long): String {
    val totalSeconds = millis / 1_000L
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

private fun formatMatchClockInput(millis: Long): String {
    val totalSeconds = millis / 1_000L
    return "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

private fun matchClockToMillis(value: String): Long? {
    val parts = value.trim().split(':')
    if (parts.size != 2) return null

    val minutes = parts[0].toLongOrNull() ?: return null
    val seconds = parts[1].toLongOrNull() ?: return null
    if (minutes < 0 || seconds !in 0..59) return null

    return (minutes * 60L + seconds) * 1_000L
}

private fun substitutionText(
    playerInName: String,
    playerOutName: String,
    fallback: String,
): String =
    if (playerInName.isNotBlank() && playerOutName.isNotBlank()) {
        "$playerInName inn, $playerOutName ut"
    } else {
        fallback
    }

private fun String.toPlayerNames(): List<String> =
    split(',')
        .map(String::trim)
        .filter(String::isNotBlank)

private fun TranscriptionEventIssue.requiredEventType(): TranscriptionEventType? = when {
    issueType.equals("STARTING_LINEUP_MISSING", ignoreCase = true) -> {
        TranscriptionEventType.STARTING_LINEUP
    }

    issueType.equals("INVALID_SUBSTITUTION_COMMAND", ignoreCase = true) -> {
        TranscriptionEventType.SUBSTITUTION
    }

    else -> null
}

private fun TranscriptionEventType.displayName(): String = when (this) {
    TranscriptionEventType.OBSERVATION -> "Observasjon"
    TranscriptionEventType.STAT -> "Statistikk"
    TranscriptionEventType.SUBSTITUTION -> "Spillerbytte"
    TranscriptionEventType.STARTING_LINEUP -> "Startoppstilling"
}

private fun TranscriptionEventIssue.displayMessage(): String =
    if (requiredEventType() == TranscriptionEventType.STARTING_LINEUP) {
        "Legg inn en startoppstilling før spillerbytter kan registreres"
    } else {
        message ?: issueType
    }
