package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Edit
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.ExistingCoachActivityUiModel
import com.example.assistenttreneren.feature.activitywizard.domain.model.MatchRosterSuggestion
import com.example.assistenttreneren.feature.activitywizard.presentation.RecordingUiModel
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold

@Composable
fun ActivityTypeStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onCreateNewActivityClicked: () -> Unit,
    onSelectExistingActivityClicked: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onActivityCategorySelected: (String) -> Unit,
    onExistingActivitySelected: (String) -> Unit,
    onMatchRosterChanged: (List<String>) -> Unit,
    onMatchRosterSuggestionSelected: (List<String>) -> Unit,
    onMatchHalfDurationChanged: (Int) -> Unit,
    onRetryLoadExistingActivitiesClicked: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    var showRosterDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.ActivityType)
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_activity_type_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onNavigateNext,
        isNextEnabled = uiState.canContinueFromActivityType,
        isBackEnabled = !uiState.isCreatingActivity && !uiState.isUpdatingExistingActivity,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onCreateNewActivityClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.wizard_create_new_activity_button))
                }
            }

            OutlinedButton(
                onClick = onSelectExistingActivityClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.wizard_select_existing_activity_button))
            }
        }

        if (uiState.isCreateActivityFormVisible) {
            CreateActivityForm(
                title = uiState.title,
                selectedActivityCategory = uiState.activityCategory,
                onTitleChanged = onTitleChanged,
                onActivityCategorySelected = onActivityCategorySelected,
                matchRoster = uiState.matchRoster,
                onEditMatchRoster = { showRosterDialog = true },
                matchHalfDurationMinutes = uiState.matchHalfDurationMinutes,
                onMatchHalfDurationChanged = onMatchHalfDurationChanged,
            )
        } else if (uiState.isExistingActivityFormVisible) {
            ExistingActivityForm(
                activities = uiState.existingActivities,
                selectedActivity = uiState.selectedExistingActivity,
                title = uiState.title,
                isLoading = uiState.isLoadingActivities,
                errorMessage = uiState.activityErrorMessage,
                isUpdatingActivity = uiState.isUpdatingExistingActivity,
                onTitleChanged = onTitleChanged,
                onExistingActivitySelected = onExistingActivitySelected,
                onRetryLoadExistingActivitiesClicked = onRetryLoadExistingActivitiesClicked,
                matchRoster = uiState.matchRoster,
                onEditMatchRoster = { showRosterDialog = true },
                matchHalfDurationMinutes = uiState.matchHalfDurationMinutes,
                onMatchHalfDurationChanged = onMatchHalfDurationChanged,
            )
        }

        if (uiState.isCreateActivityFormVisible && uiState.activityErrorMessage != null) {
            Text(
                text = uiState.activityErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (uiState.isCreatingActivity || uiState.isUpdatingExistingActivity) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(
                        if (uiState.isCreatingActivity) {
                            R.string.wizard_activity_creating_status
                        } else {
                            R.string.wizard_activity_updating_status
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (showRosterDialog) {
        MatchRosterDialog(
            initialRoster = uiState.matchRoster,
            suggestions = uiState.matchRosterSuggestions,
            isLoadingSuggestions = uiState.isLoadingMatchRosterSuggestions,
            suggestionsErrorMessage = uiState.matchRosterSuggestionsErrorMessage,
            onDismiss = { showRosterDialog = false },
            onSave = onMatchRosterChanged,
            onSuggestionSelected = onMatchRosterSuggestionSelected,
        )
    }
}

@Composable
private fun CreateActivityForm(
    title: String,
    selectedActivityCategory: String?,
    onTitleChanged: (String) -> Unit,
    onActivityCategorySelected: (String) -> Unit,
    matchRoster: List<String>,
    onEditMatchRoster: () -> Unit,
    matchHalfDurationMinutes: Int,
    onMatchHalfDurationChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        stringResource(R.string.activity_category_match),
        stringResource(R.string.activity_category_training),
        stringResource(R.string.activity_category_meeting),
        stringResource(R.string.activity_category_scouting),
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                label = {
                    Text(text = stringResource(R.string.wizard_activity_title_label))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                categories.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowCategories.forEach { category ->
                            ActivityCategoryTile(
                                label = category,
                                selected = category == selectedActivityCategory,
                                onClick = {
                                    onActivityCategorySelected(category)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            if (selectedActivityCategory == "Kamp") {
                MatchRosterButton(matchRoster, onEditMatchRoster)
                MatchHalfDurationDropdown(matchHalfDurationMinutes, onMatchHalfDurationChanged)
            }
        }
    }
}

@Composable
private fun ExistingActivityForm(
    activities: List<ExistingCoachActivityUiModel>,
    selectedActivity: ExistingCoachActivityUiModel?,
    title: String,
    isLoading: Boolean,
    errorMessage: String?,
    isUpdatingActivity: Boolean,
    onTitleChanged: (String) -> Unit,
    onExistingActivitySelected: (String) -> Unit,
    onRetryLoadExistingActivitiesClicked: () -> Unit,
    matchRoster: List<String>,
    onEditMatchRoster: () -> Unit,
    matchHalfDurationMinutes: Int,
    onMatchHalfDurationChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showActivityPicker by remember { mutableStateOf(false) }
    var isTitleEditMode by remember(selectedActivity?.activityId) { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        showActivityPicker = true
                    },
                    enabled = activities.isNotEmpty() && !isLoading && !isUpdatingActivity,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                isLoading -> stringResource(R.string.wizard_existing_activity_loading)
                                else -> selectedActivity?.dropdownLabel
                                    ?: stringResource(R.string.wizard_existing_activity_dropdown_label)
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = null,
                        )
                    }
                }

            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )

                OutlinedButton(
                    onClick = onRetryLoadExistingActivitiesClicked,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.wizard_retry_button))
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                label = {
                    Text(text = stringResource(R.string.wizard_activity_title_label))
                },
                readOnly = !isTitleEditMode,
                enabled = selectedActivity != null && !isUpdatingActivity,
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { isTitleEditMode = !isTitleEditMode },
                        enabled = selectedActivity != null && !isUpdatingActivity,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.wizard_edit_activity_title),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            RecordingsList(
                recordings = selectedActivity?.recordings.orEmpty(),
            )
            if (selectedActivity?.activityCategory == "Kamp") {
                MatchRosterButton(matchRoster, onEditMatchRoster)
                MatchHalfDurationDropdown(matchHalfDurationMinutes, onMatchHalfDurationChanged)
            }
        }
    }
    if (showActivityPicker) {
        SelectionListDialog(
            title = stringResource(R.string.wizard_existing_activity_dropdown_label),
            items = activities.map { SelectionListItem(title = it.dropdownLabel) },
            onDismiss = { showActivityPicker = false },
            onItemSelected = { index ->
                showActivityPicker = false
                onExistingActivitySelected(activities[index].activityId)
            },
        )
    }
}

@Composable
private fun MatchRosterButton(roster: List<String>, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onClick) { Text("+ Kamptropp") }
        if (roster.isEmpty()) Text("Legg inn kamptropp før du kan gå videre", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MatchHalfDurationDropdown(
    selectedMinutes: Int,
    onSelected: (Int) -> Unit,
) {
    var showDurationPicker by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { showDurationPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Omgangslengde: $selectedMinutes min",
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
            }
        }
    }
    if (showDurationPicker) {
        SelectionListDialog(
            title = "Velg omgangslengde",
            items = matchHalfDurationOptions.map { minutes ->
                SelectionListItem(if (minutes == 1) "1 minutt (test)" else "$minutes minutter")
            },
            onDismiss = { showDurationPicker = false },
            onItemSelected = { index ->
                onSelected(matchHalfDurationOptions[index])
                showDurationPicker = false
            },
        )
    }
}

private val matchHalfDurationOptions = listOf(1, 20, 25, 30, 35, 40, 45)

@Composable
private fun MatchRosterDialog(
    initialRoster: List<String>,
    suggestions: List<MatchRosterSuggestion>,
    isLoadingSuggestions: Boolean,
    suggestionsErrorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
    onSuggestionSelected: (List<String>) -> Unit,
) {
    var names by remember { mutableStateOf(initialRoster.joinToString("\n")) }
    var showSuggestionsPicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Kamptropp")
                Button(
                    onClick = { showSuggestionsPicker = true },
                    enabled = !isLoadingSuggestions && suggestions.isNotEmpty(),
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(
                        text = when {
                            isLoadingSuggestions -> "Henter..."
                            suggestions.isEmpty() -> "Ingen tidligere"
                            else -> "Tidligere tropper"
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = names,
                    onValueChange = { names = it },
                    label = { Text("Ett spillernavn per linje") },
                    modifier = Modifier.fillMaxWidth(),
                )
                suggestionsErrorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(names.lines().map(String::trim).filter(String::isNotBlank)); onDismiss() }) { Text("Lagre") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Avbryt") } },
    )
    if (showSuggestionsPicker) {
        SelectionListDialog(
            title = "Velg tidligere kamptropp",
            items = suggestions.map { suggestion ->
                SelectionListItem(
                    title = suggestion.title,
                    subtitle = suggestion.playerNames.joinToString(", "),
                )
            },
            onDismiss = { showSuggestionsPicker = false },
            onItemSelected = { index ->
                val suggestion = suggestions[index]
                names = suggestion.playerNames.joinToString("\n")
                onSuggestionSelected(suggestion.playerNames)
                showSuggestionsPicker = false
            },
        )
    }
}

private data class SelectionListItem(
    val title: String,
    val subtitle: String? = null,
)

@Composable
private fun SelectionListDialog(
    title: String,
    items: List<SelectionListItem>,
    onDismiss: () -> Unit,
    onItemSelected: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(items) { index, item ->
                    OutlinedButton(
                        onClick = { onItemSelected(index) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(item.title)
                            item.subtitle?.let { subtitle ->
                                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Avbryt") } },
    )
}

@Composable
private fun RecordingsList(
    recordings: List<RecordingUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_existing_activity_recordings_label),
            style = MaterialTheme.typography.titleMedium,
        )

        if (recordings.isEmpty()) {
            Text(
                text = stringResource(R.string.wizard_existing_activity_no_recordings),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            recordings.forEach { recording ->
                RecordingListItem(recording = recording)
            }
        }
    }
}

@Composable
private fun RecordingListItem(
    recording: RecordingUiModel,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = recording.filename,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(
                    R.string.wizard_existing_activity_recording_details,
                    recording.recordingType,
                    recording.duration,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActivityCategoryTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
            )
        }
    }
}
