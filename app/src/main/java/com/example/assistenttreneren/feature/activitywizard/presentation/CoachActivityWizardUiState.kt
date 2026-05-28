package com.example.assistenttreneren.feature.activitywizard.presentation

data class CoachActivityWizardUiState(
    val currentStep: CoachActivityWizardStep = CoachActivityWizardStep.ActivityType,
    val activityInputMode: CoachActivityInputMode? = null,
    val title: String = "",
    val activityCategory: String? = null,
    val existingActivities: List<ExistingCoachActivityUiModel> = emptyList(),
    val selectedExistingActivityId: String? = null,
) {
    val totalSteps: Int = CoachActivityWizardStep.entries.size

    val isCreateActivityFormVisible: Boolean
        get() = activityInputMode == CoachActivityInputMode.CreateNew

    val isExistingActivityFormVisible: Boolean
        get() = activityInputMode == CoachActivityInputMode.Existing

    val selectedExistingActivity: ExistingCoachActivityUiModel?
        get() = existingActivities.firstOrNull { it.activityId == selectedExistingActivityId }

    val canContinueFromActivityType: Boolean
        get() = when (activityInputMode) {
            CoachActivityInputMode.CreateNew -> title.isNotBlank() && activityCategory != null
            CoachActivityInputMode.Existing -> selectedExistingActivity != null
            null -> false
        }
}
