package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.ExistingCoachActivityUiModel
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
    onRetryLoadExistingActivitiesClicked: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.ActivityType)
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_activity_type_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onNavigateNext,
        isNextEnabled = uiState.canContinueFromActivityType,
        isBackEnabled = !uiState.isCreatingActivity,
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
            )
        } else if (uiState.isExistingActivityFormVisible) {
            ExistingActivityForm(
                activities = uiState.existingActivities,
                selectedActivity = uiState.selectedExistingActivity,
                title = uiState.title,
                isLoading = uiState.isLoadingActivities,
                errorMessage = uiState.activityErrorMessage,
                onExistingActivitySelected = onExistingActivitySelected,
                onRetryLoadExistingActivitiesClicked = onRetryLoadExistingActivitiesClicked,
            )
        }

        if (uiState.isCreateActivityFormVisible && uiState.activityErrorMessage != null) {
            Text(
                text = uiState.activityErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (uiState.isCreatingActivity) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.wizard_activity_creating_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CreateActivityForm(
    title: String,
    selectedActivityCategory: String?,
    onTitleChanged: (String) -> Unit,
    onActivityCategorySelected: (String) -> Unit,
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
    onExistingActivitySelected: (String) -> Unit,
    onRetryLoadExistingActivitiesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

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
                        isMenuExpanded = true
                    },
                    enabled = activities.isNotEmpty() && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = when {
                            isLoading -> stringResource(R.string.wizard_existing_activity_loading)
                            else -> selectedActivity?.dropdownLabel
                                ?: stringResource(R.string.wizard_existing_activity_dropdown_label)
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = {
                        isMenuExpanded = false
                    },
                    modifier = Modifier.widthIn(max = 360.dp),
                ) {
                    activities.forEach { activity ->
                        DropdownMenuItem(
                            text = {
                                Text(text = activity.dropdownLabel)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onExistingActivitySelected(activity.activityId)
                            },
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
                onValueChange = {},
                label = {
                    Text(text = stringResource(R.string.wizard_activity_title_label))
                },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            RecordingsList(
                recordings = selectedActivity?.recordings.orEmpty(),
            )
        }
    }
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
