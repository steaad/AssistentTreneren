package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus
import com.example.assistenttreneren.feature.upload.presentation.UploadJobUiModel
import com.example.assistenttreneren.feature.upload.presentation.UploadRecordingUiModel
import com.example.assistenttreneren.feature.upload.presentation.UploadStepUiState
import com.example.assistenttreneren.feature.upload.presentation.UploadStepViewModel

@Composable
fun UploadStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    uploadStepViewModel: UploadStepViewModel = hiltViewModel(),
) {
    val uploadUiState by uploadStepViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.Upload)
    }

    LaunchedEffect(uiState.selectedActivityId) {
        uploadStepViewModel.loadActivity(uiState.selectedActivityId)
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_upload_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onNavigateNext,
    ) {
        UploadStepContent(
            uiState = uploadUiState,
            onRecordingSelected = uploadStepViewModel::onRecordingSelected,
            onUploadClicked = uploadStepViewModel::uploadSelectedRecording,
            onRetryUploadClicked = uploadStepViewModel::retryUpload,
        )
    }
}

@Composable
private fun UploadStepContent(
    uiState: UploadStepUiState,
    onRecordingSelected: (String) -> Unit,
    onUploadClicked: () -> Unit,
    onRetryUploadClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecordingsSection(
            recordings = uiState.recordings,
            selectedRecordingId = uiState.selectedRecordingId,
            onRecordingSelected = onRecordingSelected,
        )

        Button(
            onClick = onUploadClicked,
            enabled = uiState.canUploadSelectedRecording,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.upload_selected_recording_button))
        }

        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        UploadJobsSection(
            uploadJobs = uiState.uploadJobs,
            onRetryUploadClicked = onRetryUploadClicked,
        )
    }
}

@Composable
private fun RecordingsSection(
    recordings: List<UploadRecordingUiModel>,
    selectedRecordingId: String?,
    onRecordingSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.upload_recordings_title),
            style = MaterialTheme.typography.titleMedium,
        )

        if (recordings.isEmpty()) {
            Text(
                text = stringResource(R.string.upload_no_recordings),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            recordings.forEach { recording ->
                RecordingUploadItem(
                    recording = recording,
                    selected = recording.recordingId == selectedRecordingId,
                    onClick = {
                        onRecordingSelected(recording.recordingId)
                    },
                )
            }
        }
    }
}

@Composable
private fun RecordingUploadItem(
    recording: UploadRecordingUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = recording.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = stringResource(
                    R.string.upload_recording_details,
                    recording.mediaType.asDisplayText(),
                    recording.subCategory,
                    formatDuration(recording.durationMillis),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (recording.hasActiveUpload) {
                Text(
                    text = stringResource(R.string.upload_recording_active_job),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun UploadJobsSection(
    uploadJobs: List<UploadJobUiModel>,
    onRetryUploadClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.upload_jobs_title),
            style = MaterialTheme.typography.titleMedium,
        )

        if (uploadJobs.isEmpty()) {
            Text(
                text = stringResource(R.string.upload_no_jobs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            uploadJobs.forEach { uploadJob ->
                UploadJobItem(
                    uploadJob = uploadJob,
                    onRetryUploadClicked = onRetryUploadClicked,
                )
            }
        }
    }
}

@Composable
private fun UploadJobItem(
    uploadJob: UploadJobUiModel,
    onRetryUploadClicked: (String) -> Unit,
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = uploadJob.recordingDisplayName,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = uploadJob.status.asDisplayText(),
                style = MaterialTheme.typography.bodyMedium,
                color = uploadJob.status.asStatusColor(),
            )
            uploadJob.statusMessage?.let { statusMessage ->
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            uploadJob.progressPercent?.let { progress ->
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            uploadJob.lastError?.let { lastError ->
                Text(
                    text = lastError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (uploadJob.canRetry) {
                OutlinedButton(
                    onClick = {
                        onRetryUploadClicked(uploadJob.uploadJobId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.upload_retry_button))
                }
            }
        }
    }
}

@Composable
private fun UploadStatus.asStatusColor() =
    when (this) {
        UploadStatus.Completed -> MaterialTheme.colorScheme.primary
        UploadStatus.Failed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

private fun RecordingMediaType.asDisplayText(): String =
    when (this) {
        RecordingMediaType.Audio -> "Lyd"
        RecordingMediaType.Video -> "Video"
    }

private fun UploadStatus.asDisplayText(): String =
    when (this) {
        UploadStatus.Queued -> "I kø"
        UploadStatus.Uploading -> "Laster opp"
        UploadStatus.ProcessingAudio -> "Behandler lyd"
        UploadStatus.Transcribing -> "Transkriberer"
        UploadStatus.Completed -> "Fullført"
        UploadStatus.Failed -> "Feilet"
    }

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
