package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold

@Composable
fun ActivityTypeStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onCreateNewActivityClicked: () -> Unit,
    onSelectExistingActivityClicked: () -> Unit,
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

        Text(
            text = stringResource(R.string.wizard_activity_type_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
