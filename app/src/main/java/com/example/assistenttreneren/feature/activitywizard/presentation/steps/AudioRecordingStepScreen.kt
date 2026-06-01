package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSubCategory
import com.example.assistenttreneren.feature.recording.presentation.RecordingUiState
import com.example.assistenttreneren.feature.recording.presentation.RecordingViewModel
import kotlinx.coroutines.delay

@Composable
fun AudioRecordingStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    recordingViewModel: RecordingViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.AudioRecording)
    }

    val recordingUiState by recordingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val category = uiState.activityCategory
        ?: uiState.selectedExistingActivity?.activityCategory
    val subCategories = RecordingSubCategory.forActivityCategory(category)
    val activityId = uiState.selectedExistingActivityId
    var permissionMessageVisible by remember { mutableStateOf(false) }
    var shouldStartAfterPermissionGrant by remember { mutableStateOf(false) }
    var elapsedRecordingMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(recordingUiState.isRecording, recordingUiState.recordingStartedAtMillis) {
        val startedAtMillis = recordingUiState.recordingStartedAtMillis
        if (!recordingUiState.isRecording || startedAtMillis == null) {
            elapsedRecordingMillis = 0L
            return@LaunchedEffect
        }

        while (true) {
            elapsedRecordingMillis = (System.currentTimeMillis() - startedAtMillis)
                .coerceAtLeast(0L)
            delay(1_000L)
        }
    }

    LaunchedEffect(category, recordingUiState.subCategory) {
        val selectedSubCategoryIsValid = subCategories.any { subCategory ->
            subCategory.displayName == recordingUiState.subCategory
        }

        if (recordingUiState.subCategory.isNotBlank() && !selectedSubCategoryIsValid) {
            recordingViewModel.clearSubCategory()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val hasRequiredPermissions = requiredRecordingPermissions().all { permission ->
            grants[permission] == true || ContextCompat.checkSelfPermission(
                context,
                permission,
            ) == PackageManager.PERMISSION_GRANTED
        }

        permissionMessageVisible = !hasRequiredPermissions
        if (hasRequiredPermissions && shouldStartAfterPermissionGrant && category != null) {
            recordingViewModel.startRecording(
                category = category,
                activityId = activityId,
            )
        }
        shouldStartAfterPermissionGrant = false
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_audio_recording_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onNavigateNext,
        isBackEnabled = !recordingUiState.isRecording,
        isNextEnabled = !recordingUiState.isRecording,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.wizard_audio_recording_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SubCategoryTiles(
                subCategories = subCategories,
                selectedSubCategory = recordingUiState.subCategory,
                enabled = !recordingUiState.isRecording,
                onSubCategorySelected = recordingViewModel::onSubCategorySelected,
            )

            RecordingControlPanel(
                recordingUiState = recordingUiState,
                permissionMessageVisible = permissionMessageVisible,
                elapsedRecordingMillis = elapsedRecordingMillis,
                canStartRecording = category != null,
                onStartRecording = startRecording@{
                    if (category == null) {
                        permissionMessageVisible = true
                        return@startRecording
                    }

                    if (hasRecordingPermissions(context)) {
                        recordingViewModel.startRecording(
                            category = category,
                            activityId = activityId,
                        )
                    } else {
                        shouldStartAfterPermissionGrant = true
                        permissionLauncher.launch(requiredRecordingPermissions())
                    }
                },
                onStopRecording = recordingViewModel::stopRecording,
            )
        }
    }
}

@Composable
private fun RecordingControlPanel(
    recordingUiState: RecordingUiState,
    permissionMessageVisible: Boolean,
    elapsedRecordingMillis: Long,
    canStartRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onStartRecording,
                    enabled = recordingUiState.canStartRecording && canStartRecording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_start_button))
                }

                OutlinedButton(
                    onClick = onStopRecording,
                    enabled = recordingUiState.canStopRecording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_stop_button))
                }
            }

            Text(
                text = formatStopwatchTime(elapsedRecordingMillis),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            RecordingStatusText(
                recordingUiState = recordingUiState,
                permissionMessageVisible = permissionMessageVisible,
            )
        }
    }
}

@Composable
private fun SubCategoryTiles(
    subCategories: List<RecordingSubCategory>,
    selectedSubCategory: String,
    enabled: Boolean,
    onSubCategorySelected: (RecordingSubCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        subCategories.chunked(2).forEach { rowSubCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowSubCategories.forEach { subCategory ->
                    SubCategoryTile(
                        label = subCategory.displayName,
                        selected = subCategory.displayName == selectedSubCategory,
                        enabled = enabled,
                        onClick = {
                            onSubCategorySelected(subCategory)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (rowSubCategories.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SubCategoryTile(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
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
            containerColor = containerColor,
            disabledContainerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = contentColor,
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
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RecordingStatusText(
    recordingUiState: com.example.assistenttreneren.feature.recording.presentation.RecordingUiState,
    permissionMessageVisible: Boolean,
) {
    val text = when {
        permissionMessageVisible -> stringResource(R.string.recording_permission_required)
        recordingUiState.errorMessage != null -> recordingUiState.errorMessage
        recordingUiState.isRecording -> stringResource(
            R.string.recording_active_status,
            recordingUiState.activeDisplayName.orEmpty(),
        )
        recordingUiState.completedRecording != null -> stringResource(
            R.string.recording_completed_status,
            recordingUiState.completedRecording.displayName,
        )
        else -> stringResource(R.string.recording_idle_status)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun hasRecordingPermissions(context: android.content.Context): Boolean =
    requiredRecordingPermissions().all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

private fun requiredRecordingPermissions(): Array<String> =
    buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

private fun formatStopwatchTime(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return "%02d : %02d : %02d".format(hours, minutes, seconds)
}
