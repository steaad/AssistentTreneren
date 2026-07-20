package com.example.assistenttreneren.feature.activitywizard.presentation

data class CoachActivityWizardUiState(
    val currentStep: CoachActivityWizardStep = CoachActivityWizardStep.ActivityType,
    val activityInputMode: CoachActivityInputMode? = null,
    val title: String = "",
    val activityCategory: String? = null,
    val existingActivities: List<ExistingCoachActivityUiModel> = emptyList(),
    val selectedExistingActivityId: String? = null,
    val selectedActivityId: String? = null,
    val isLoadingActivities: Boolean = false,
    val isCreatingActivity: Boolean = false,
    val isUpdatingExistingActivity: Boolean = false,
    val activityErrorMessage: String? = null,
) {
    val totalSteps: Int = CoachActivityWizardStep.entries.size

    val isCreateActivityFormVisible: Boolean
        get() = activityInputMode == CoachActivityInputMode.CreateNew

    val isExistingActivityFormVisible: Boolean
        get() = activityInputMode == CoachActivityInputMode.Existing

    val selectedExistingActivity: ExistingCoachActivityUiModel?
        get() = existingActivities.firstOrNull { it.activityId == selectedExistingActivityId }

    val canContinueFromActivityType: Boolean
        get() = !isLoadingActivities && !isCreatingActivity && !isUpdatingExistingActivity && when (activityInputMode) {
            CoachActivityInputMode.CreateNew -> title.isNotBlank() && activityCategory != null
            CoachActivityInputMode.Existing -> selectedActivityId != null && selectedExistingActivity != null
            null -> false
        }
}
