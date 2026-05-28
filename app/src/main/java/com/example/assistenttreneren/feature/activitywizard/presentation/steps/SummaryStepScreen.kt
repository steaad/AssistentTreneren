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
fun SummaryStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.Summary)
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_summary_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onFinish,
        nextButtonText = stringResource(R.string.wizard_finish_button),
    ) {
        Text(
            text = stringResource(R.string.wizard_summary_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
