package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.feature.activitywizard.domain.model.MatchRosterSuggestion

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
    val matchRoster: List<String> = emptyList(),
    val matchRosterSuggestions: List<MatchRosterSuggestion> = emptyList(),
    val isLoadingMatchRosterSuggestions: Boolean = false,
    val matchRosterSuggestionsErrorMessage: String? = null,
    val matchHalfDurationMinutes: Int = 45,
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
            CoachActivityInputMode.CreateNew -> title.isNotBlank() && activityCategory != null && (activityCategory != "Kamp" || matchRoster.isNotEmpty())
            CoachActivityInputMode.Existing -> selectedActivityId != null && selectedExistingActivity != null && (activityCategory != "Kamp" || matchRoster.isNotEmpty())
            null -> false
        }
}
