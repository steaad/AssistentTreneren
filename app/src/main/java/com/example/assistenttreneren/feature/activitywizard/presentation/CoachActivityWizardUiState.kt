package com.example.assistenttreneren.feature.activitywizard.presentation

data class CoachActivityWizardUiState(
    val currentStep: CoachActivityWizardStep = CoachActivityWizardStep.ActivityType,
) {
    val totalSteps: Int = CoachActivityWizardStep.entries.size
}
