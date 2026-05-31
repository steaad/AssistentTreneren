package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.recording.presentation.RecordingViewModel

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
    val activityId = uiState.selectedExistingActivityId
    var permissionMessageVisible by remember { mutableStateOf(false) }
    var shouldStartAfterPermissionGrant by remember { mutableStateOf(false) }
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

            OutlinedTextField(
                value = recordingUiState.subCategory,
                onValueChange = recordingViewModel::onSubCategoryChanged,
                enabled = !recordingUiState.isRecording,
                label = {
                    Text(text = stringResource(R.string.recording_sub_category_label))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        if (category == null) {
                            permissionMessageVisible = true
                            return@Button
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
                    enabled = recordingUiState.canStartRecording && category != null,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_start_button))
                }

                OutlinedButton(
                    onClick = recordingViewModel::stopRecording,
                    enabled = recordingUiState.canStopRecording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_stop_button))
                }
            }

            RecordingStatusText(
                recordingUiState = recordingUiState,
                permissionMessageVisible = permissionMessageVisible,
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
