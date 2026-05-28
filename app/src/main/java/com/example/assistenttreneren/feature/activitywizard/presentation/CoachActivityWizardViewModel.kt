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
            currentState.copy(activityInputMode = CoachActivityInputMode.Existing)
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
}
