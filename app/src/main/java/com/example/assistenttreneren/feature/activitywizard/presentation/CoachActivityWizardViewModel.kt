package com.example.assistenttreneren.feature.activitywizard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.BuildConfig
import com.example.assistenttreneren.core.auth.SessionManager
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import com.example.assistenttreneren.feature.activitywizard.domain.model.Recording
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityError
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityResult
import com.example.assistenttreneren.feature.activitywizard.domain.repository.LocalCoachActivityRepository
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.CreateCoachActivityUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.GetCoachActivitiesUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.UpdateCoachActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class CoachActivityWizardViewModel @Inject constructor(
    private val getCoachActivitiesUseCase: GetCoachActivitiesUseCase,
    private val createCoachActivityUseCase: CreateCoachActivityUseCase,
    private val updateCoachActivityUseCase: UpdateCoachActivityUseCase,
    private val localCoachActivityRepository: LocalCoachActivityRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoachActivityWizardUiState())
    val uiState: StateFlow<CoachActivityWizardUiState> = _uiState.asStateFlow()

    fun onStepOpened(step: CoachActivityWizardStep) {
        _uiState.update { currentState ->
            currentState.copy(currentStep = step)
        }
    }

    fun onCreateNewActivityClicked() {
        _uiState.update { currentState ->
            currentState.copy(
                activityInputMode = CoachActivityInputMode.CreateNew,
                selectedExistingActivityId = null,
                selectedActivityId = null,
                activityErrorMessage = null,
            )
        }
    }

    fun onSelectExistingActivityClicked() {
        _uiState.update { currentState ->
            currentState.copy(
                activityInputMode = CoachActivityInputMode.Existing,
                selectedActivityId = currentState.selectedExistingActivityId,
                activityErrorMessage = null,
            )
        }
        loadExistingActivities()
    }

    fun onTitleChanged(title: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = title,
                activityErrorMessage = null,
            )
        }
    }

    fun onActivityCategorySelected(activityCategory: String) {
        _uiState.update { currentState ->
            currentState.copy(
                activityCategory = activityCategory,
                activityErrorMessage = null,
            )
        }
    }

    fun onExistingActivitySelected(activityId: String) {
        _uiState.update { currentState ->
            val selectedActivity = currentState.existingActivities.firstOrNull {
                it.activityId == activityId
            }

            currentState.copy(
                selectedExistingActivityId = selectedActivity?.activityId,
                selectedActivityId = selectedActivity?.activityId,
                title = selectedActivity?.title.orEmpty(),
                activityCategory = selectedActivity?.activityCategory,
                activityErrorMessage = null,
            )
        }
    }

    fun onRetryLoadExistingActivitiesClicked() {
        loadExistingActivities()
    }

    fun onContinueFromActivityType(
        onNavigateNext: () -> Unit,
    ) {
        val currentState = uiState.value
        when (currentState.activityInputMode) {
            CoachActivityInputMode.CreateNew -> createActivityAndNavigate(onNavigateNext)
            CoachActivityInputMode.Existing -> {
                updateExistingActivityAndNavigate(onNavigateNext)
            }

            null -> Unit
        }
    }

    private fun loadExistingActivities() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingActivities = true,
                    activityErrorMessage = null,
                )
            }

            val cachedActivities = localCoachActivityRepository.observeActivities()
                .first()
                .map { it.toExistingCoachActivityUiModel() }

            if (cachedActivities.isNotEmpty()) {
                _uiState.update {
                    it.copy(existingActivities = cachedActivities)
                }
            }

            if (isBackendBypassActive()) {
                _uiState.update {
                    it.copy(
                        isLoadingActivities = false,
                        activityErrorMessage = null,
                    )
                }
                return@launch
            }

            when (val result = getCoachActivitiesUseCase()) {
                is CoachActivityResult.Success -> {
                    localCoachActivityRepository.saveActivities(result.data)
                    _uiState.update {
                        it.copy(
                            existingActivities = result.data.map { activity ->
                                activity.toExistingCoachActivityUiModel()
                            },
                            isLoadingActivities = false,
                            activityErrorMessage = null,
                        )
                    }
                }

                is CoachActivityResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoadingActivities = false,
                            activityErrorMessage = if (cachedActivities.isEmpty()) {
                                result.error.toUserMessage()
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    private fun createActivityAndNavigate(
        onNavigateNext: () -> Unit,
    ) {
        val currentState = uiState.value
        val title = currentState.title.trim()
        val activityCategory = currentState.activityCategory

        if (title.isBlank() || activityCategory == null || currentState.isCreatingActivity) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingActivity = true,
                    activityErrorMessage = null,
                )
            }

            if (isBackendBypassActive()) {
                createDebugActivityAndNavigate(
                    title = title,
                    activityCategory = activityCategory,
                    onNavigateNext = onNavigateNext,
                )
                return@launch
            }

            val createdActivity = when (val createResult = createCoachActivityUseCase()) {
                is CoachActivityResult.Success -> createResult.data
                is CoachActivityResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isCreatingActivity = false,
                            activityErrorMessage = createResult.error.toUserMessage(),
                        )
                    }
                    return@launch
                }
            }

            when (
                val updateResult = updateCoachActivityUseCase(
                    activityId = createdActivity.activityId,
                    activityCategory = activityCategory,
                    title = title,
                )
            ) {
                is CoachActivityResult.Success -> {
                    localCoachActivityRepository.saveActivity(updateResult.data)
                    _uiState.update {
                        it.copy(
                            selectedActivityId = updateResult.data.activityId,
                            selectedExistingActivityId = null,
                            title = updateResult.data.title.orEmpty(),
                            activityCategory = updateResult.data.activityCategory,
                            isCreatingActivity = false,
                            activityErrorMessage = null,
                        )
                    }
                    onNavigateNext()
                }

                is CoachActivityResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isCreatingActivity = false,
                            activityErrorMessage = updateResult.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun updateExistingActivityAndNavigate(
        onNavigateNext: () -> Unit,
    ) {
        val currentState = uiState.value
        val selectedActivity = currentState.selectedExistingActivity ?: return
        val updatedTitle = currentState.title.trim()

        if (currentState.isUpdatingExistingActivity) {
            return
        }

        if (updatedTitle.isBlank()) {
            _uiState.update {
                it.copy(activityErrorMessage = "Aktiviteten må ha en tittel.")
            }
            return
        }

        if (updatedTitle == selectedActivity.title.orEmpty().trim()) {
            onNavigateNext()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingExistingActivity = true,
                    activityErrorMessage = null,
                )
            }

            if (isBackendBypassActive()) {
                val updatedActivity = CoachActivity(
                    activityId = selectedActivity.activityId,
                    activityCategory = selectedActivity.activityCategory,
                    title = updatedTitle,
                    recordings = selectedActivity.recordings.map { recording ->
                        Recording(
                            id = recording.id,
                            recordingType = recording.recordingType,
                            filename = recording.filename,
                            duration = recording.duration,
                        )
                    },
                )
                localCoachActivityRepository.saveActivity(updatedActivity)
                updateExistingActivityStateAndNavigate(updatedActivity, onNavigateNext)
                return@launch
            }

            when (
                val result = updateCoachActivityUseCase(
                    activityId = selectedActivity.activityId,
                    activityCategory = null,
                    title = updatedTitle,
                )
            ) {
                is CoachActivityResult.Success -> {
                    localCoachActivityRepository.saveActivity(result.data)
                    updateExistingActivityStateAndNavigate(result.data, onNavigateNext)
                }

                is CoachActivityResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingExistingActivity = false,
                            activityErrorMessage = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun updateExistingActivityStateAndNavigate(
        activity: CoachActivity,
        onNavigateNext: () -> Unit,
    ) {
        _uiState.update {
            it.copy(
                existingActivities = it.existingActivities.map { existingActivity ->
                    if (existingActivity.activityId == activity.activityId) {
                        activity.toExistingCoachActivityUiModel()
                    } else {
                        existingActivity
                    }
                },
                title = activity.title.orEmpty(),
                activityCategory = activity.activityCategory,
                isUpdatingExistingActivity = false,
                activityErrorMessage = null,
            )
        }
        onNavigateNext()
    }

    private fun isBackendBypassActive(): Boolean =
        BuildConfig.DEBUG && sessionManager.isBackendBypassActive.value

    private suspend fun createDebugActivityAndNavigate(
        title: String,
        activityCategory: String,
        onNavigateNext: () -> Unit,
    ) {
        val activity = CoachActivity(
            activityId = "debug-local-${UUID.randomUUID()}",
            activityCategory = activityCategory,
            title = title,
            recordings = emptyList(),
        )

        localCoachActivityRepository.saveActivity(activity)
        _uiState.update {
            it.copy(
                selectedActivityId = activity.activityId,
                selectedExistingActivityId = null,
                title = activity.title.orEmpty(),
                activityCategory = activity.activityCategory,
                isCreatingActivity = false,
                activityErrorMessage = null,
            )
        }
        onNavigateNext()
    }

    private fun CoachActivity.toExistingCoachActivityUiModel(): ExistingCoachActivityUiModel =
        ExistingCoachActivityUiModel(
            activityId = activityId,
            activityCategory = activityCategory.orEmpty(),
            title = title.orEmpty(),
            recordings = recordings.map { it.toRecordingUiModel() },
        )

    private fun Recording.toRecordingUiModel(): RecordingUiModel =
        RecordingUiModel(
            id = id,
            recordingType = recordingType,
            filename = filename,
            duration = duration,
        )

    private fun CoachActivityError.toUserMessage(): String =
        when (this) {
            CoachActivityError.InvalidInput -> "Aktiviteten mangler påkrevd informasjon."
            CoachActivityError.InvalidServerResponse -> "Serveren returnerte ugyldige aktivitetsdata."
            CoachActivityError.NetworkUnavailable -> "Ingen nettverkstilkobling. Prøv igjen senere."
            CoachActivityError.NotFound -> "Aktiviteten finnes ikke lenger."
            CoachActivityError.Unauthorized -> "Du må logge inn på nytt."
            is CoachActivityError.ServerError -> "Serverfeil ($code). Prøv igjen senere."
            is CoachActivityError.Unexpected -> "Noe gikk galt. Prøv igjen."
        }
}
