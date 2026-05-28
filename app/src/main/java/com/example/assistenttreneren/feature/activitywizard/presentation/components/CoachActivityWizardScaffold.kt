package com.example.assistenttreneren.feature.activitywizard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState

@Composable
fun CoachActivityWizardScaffold(
    title: String,
    uiState: CoachActivityWizardUiState,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    isNextEnabled: Boolean = true,
    nextButtonText: String = stringResource(R.string.wizard_next_button),
    content: @Composable () -> Unit,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.wizard_step_counter,
                    uiState.currentStep.number,
                    uiState.totalSteps,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LinearProgressIndicator(
                progress = { uiState.currentStep.number / uiState.totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                content()
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.navigate_back_button))
                }

                Button(
                    onClick = onNavigateNext,
                    enabled = isNextEnabled,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = nextButtonText)
                }
            }
        }
    }
}
