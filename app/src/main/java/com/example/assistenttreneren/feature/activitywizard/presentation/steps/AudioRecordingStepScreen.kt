package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold

@Composable
fun AudioRecordingStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.AudioRecording)
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_audio_recording_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onNavigateNext,
    ) {
        Text(
            text = stringResource(R.string.wizard_audio_recording_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
