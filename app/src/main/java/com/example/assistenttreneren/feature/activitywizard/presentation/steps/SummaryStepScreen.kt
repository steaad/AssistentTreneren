package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.SummaryRecordingUiModel
import com.example.assistenttreneren.feature.activitywizard.presentation.SummaryStepUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.SummaryStepViewModel
import com.example.assistenttreneren.feature.activitywizard.presentation.SummaryUploadJobUiModel
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus

@Composable
fun SummaryStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit,
    summaryStepViewModel: SummaryStepViewModel = hiltViewModel(),
) {
    val summaryUiState by summaryStepViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.Summary)
    }

    LaunchedEffect(uiState.selectedActivityId) {
        summaryStepViewModel.loadActivity(uiState.selectedActivityId)
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_summary_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onFinish,
        nextButtonText = stringResource(R.string.wizard_finish_button),
    ) {
        SummaryStepContent(
            wizardUiState = uiState,
            summaryUiState = summaryUiState,
        )
    }
}

@Composable
private fun SummaryStepContent(
    wizardUiState: CoachActivityWizardUiState,
    summaryUiState: SummaryStepUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ActivitySummaryCard(
            title = wizardUiState.title.ifBlank {
                stringResource(R.string.summary_missing_title)
            },
            category = wizardUiState.activityCategory
                ?: stringResource(R.string.summary_missing_category),
            recordingCount = summaryUiState.recordings.size,
            audioCount = summaryUiState.audioRecordingCount,
            videoCount = summaryUiState.videoRecordingCount,
            completedUploadCount = summaryUiState.completedUploadCount,
            failedUploadCount = summaryUiState.failedUploadCount,
        )

        RecordingsSummarySection(recordings = summaryUiState.recordings)

        UploadSummarySection(uploadJobs = summaryUiState.uploadJobs)

        Text(
            text = stringResource(R.string.summary_finish_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivitySummaryCard(
    title: String,
    category: String,
    recordingCount: Int,
    audioCount: Int,
    videoCount: Int,
    completedUploadCount: Int,
    failedUploadCount: Int,
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.summary_activity_counts,
                    recordingCount,
                    audioCount,
                    videoCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    R.string.summary_upload_counts,
                    completedUploadCount,
                    failedUploadCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecordingsSummarySection(
    recordings: List<SummaryRecordingUiModel>,
    modifier: Modifier = Modifier,
) {
    SummarySection(
        title = stringResource(R.string.summary_recordings_title),
        emptyText = stringResource(R.string.summary_no_recordings),
        isEmpty = recordings.isEmpty(),
        modifier = modifier,
    ) {
        recordings.forEach { recording ->
            SummaryRecordingItem(recording = recording)
        }
    }
}

@Composable
private fun UploadSummarySection(
    uploadJobs: List<SummaryUploadJobUiModel>,
    modifier: Modifier = Modifier,
) {
    SummarySection(
        title = stringResource(R.string.summary_uploads_title),
        emptyText = stringResource(R.string.summary_no_uploads),
        isEmpty = uploadJobs.isEmpty(),
        modifier = modifier,
    ) {
        uploadJobs.forEach { uploadJob ->
            SummaryUploadItem(uploadJob = uploadJob)
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    emptyText: String,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        if (isEmpty) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            content()
        }
    }
}

@Composable
private fun SummaryRecordingItem(
    recording: SummaryRecordingUiModel,
    modifier: Modifier = Modifier,
) {
    SummaryItemCard(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = recording.displayName,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(
                    R.string.summary_recording_details,
                    recording.mediaType.asDisplayText(),
                    recording.subCategory,
                    formatDuration(recording.durationMillis),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RecordingTypeIcon(mediaType = recording.mediaType)
    }
}

@Composable
private fun SummaryUploadItem(
    uploadJob: SummaryUploadJobUiModel,
    modifier: Modifier = Modifier,
) {
    SummaryItemCard(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
        }

        RecordingTypeIcon(mediaType = uploadJob.mediaType)
    }
}

@Composable
private fun SummaryItemCard(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun RecordingTypeIcon(
    mediaType: RecordingMediaType,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = when (mediaType) {
            RecordingMediaType.Audio -> Icons.Outlined.Mic
            RecordingMediaType.Video -> Icons.Outlined.Videocam
        },
        contentDescription = mediaType.asDisplayText(),
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(28.dp),
    )
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
