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
}
