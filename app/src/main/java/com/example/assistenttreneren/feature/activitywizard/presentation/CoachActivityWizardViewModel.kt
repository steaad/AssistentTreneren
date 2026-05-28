package com.example.assistenttreneren.feature.activitywizard.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class CoachActivityWizardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CoachActivityWizardUiState())
    val uiState: StateFlow<CoachActivityWizardUiState> = _uiState.asStateFlow()

    fun onStepOpened(step: CoachActivityWizardStep) {
        _uiState.update { currentState ->
            currentState.copy(currentStep = step)
        }
    }

    fun onCreateNewActivityClicked() {
        _uiState.update { currentState ->
            currentState.copy(activityInputMode = CoachActivityInputMode.CreateNew)
        }
    }

    fun onSelectExistingActivityClicked() {
        _uiState.update { currentState ->
            currentState.copy(
                activityInputMode = CoachActivityInputMode.Existing,
                existingActivities = currentState.existingActivities.ifEmpty {
                    sampleExistingActivities
                },
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { currentState ->
            currentState.copy(title = title)
        }
    }

    fun onActivityCategorySelected(activityCategory: String) {
        _uiState.update { currentState ->
            currentState.copy(activityCategory = activityCategory)
        }
    }

    fun onExistingActivitySelected(activityId: String) {
        _uiState.update { currentState ->
            val selectedActivity = currentState.existingActivities.firstOrNull {
                it.activityId == activityId
            }

            currentState.copy(
                selectedExistingActivityId = selectedActivity?.activityId,
                title = selectedActivity?.title.orEmpty(),
                activityCategory = selectedActivity?.activityCategory,
            )
        }
    }

    private companion object {
        val sampleExistingActivities = listOf(
            ExistingCoachActivityUiModel(
                activityId = "activity-1",
                activityCategory = "Kamp",
                title = "G14 mot Nordstrand",
                recordings = listOf(
                    RecordingUiModel(
                        id = "recording-1",
                        recordingType = "Audio",
                        filename = "g14-nordstrand-1.m4a",
                        duration = 1840,
                    ),
                    RecordingUiModel(
                        id = "recording-2",
                        recordingType = "Audio",
                        filename = "g14-nordstrand-2.m4a",
                        duration = 920,
                    ),
                ),
            ),
            ExistingCoachActivityUiModel(
                activityId = "activity-2",
                activityCategory = "Trening",
                title = "Pasningsokt senior",
                recordings = listOf(
                    RecordingUiModel(
                        id = "recording-3",
                        recordingType = "Audio",
                        filename = "pasningsokt-senior.m4a",
                        duration = 2700,
                    ),
                ),
            ),
            ExistingCoachActivityUiModel(
                activityId = "activity-3",
                activityCategory = "Speiding",
                title = "Observasjon høyreback",
            ),
        )
    }
}
