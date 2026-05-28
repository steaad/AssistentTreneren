package com.example.assistenttreneren.feature.activitywizard.presentation

data class ExistingCoachActivityUiModel(
    val activityId: String,
    val activityCategory: String?,
    val title: String?,
    val recordings: List<RecordingUiModel> = emptyList(),
) {
    val dropdownLabel: String
        get() = listOfNotNull(
            activityCategory?.takeIf { it.isNotBlank() },
            title?.takeIf { it.isNotBlank() },
        ).joinToString(separator = " - ")
            .ifBlank { activityId }
}
