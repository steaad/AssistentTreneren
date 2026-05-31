package com.example.assistenttreneren.feature.recording.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.feature.recording.domain.model.RecordingStatus
import com.example.assistenttreneren.feature.recording.domain.usecase.GetRecordingStatusUseCase
import com.example.assistenttreneren.feature.recording.domain.usecase.StartRecordingUseCase
import com.example.assistenttreneren.feature.recording.domain.usecase.StopRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecordingViewModel @Inject constructor(
    getRecordingStatusUseCase: GetRecordingStatusUseCase,
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecordingStatusUseCase().collectLatest { status ->
                _uiState.update { currentState ->
                    when (status) {
                        RecordingStatus.Idle -> currentState.copy(
                            isRecording = false,
                            activeDisplayName = null,
                        )

                        is RecordingStatus.Recording -> currentState.copy(
                            isRecording = true,
                            activeDisplayName = status.displayName,
                            errorMessage = null,
                        )

                        is RecordingStatus.Completed -> currentState.copy(
                            isRecording = false,
                            activeDisplayName = null,
                            completedRecording = status.session,
                            errorMessage = null,
                        )

                        is RecordingStatus.Error -> currentState.copy(
                            isRecording = false,
                            activeDisplayName = null,
                            errorMessage = status.message,
                        )
                    }
                }
            }
        }
    }

    fun onSubCategoryChanged(subCategory: String) {
        _uiState.update { currentState ->
            currentState.copy(subCategory = subCategory)
        }
    }

    fun startRecording(
        category: String,
        activityId: String?,
    ) {
        val subCategory = uiState.value.subCategory.trim()
        if (subCategory.isBlank()) {
            return
        }

        startRecordingUseCase(
            category = category,
            subCategory = subCategory,
            activityId = activityId,
        )
    }

    fun stopRecording() {
        stopRecordingUseCase()
    }
}
