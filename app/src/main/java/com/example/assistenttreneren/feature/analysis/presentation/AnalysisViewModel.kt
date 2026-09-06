package com.example.assistenttreneren.feature.analysis.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisCandidateState
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowError
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowResult
import com.example.assistenttreneren.feature.analysis.domain.usecase.GetAnalysisCandidatesUseCase
import com.example.assistenttreneren.feature.analysis.domain.usecase.GetAnalysisUseCase
import com.example.assistenttreneren.feature.analysis.domain.usecase.StartAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getAnalysisCandidates: GetAnalysisCandidatesUseCase,
    private val startAnalysis: StartAnalysisUseCase,
    private val getAnalysis: GetAnalysisUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        refreshCandidates()
    }

    fun refreshCandidates() {
        refreshCandidates(errorMessage = null)
    }

    fun onActivitySelected(activityId: String) {
        pollingJob?.cancel()
        _uiState.update {
            it.copy(
                selectedActivityId = activityId,
                activeAnalysis = null,
                completedAnalysis = null,
                errorMessage = null,
            )
        }
        startPollingForSelectedCandidate()
    }

    fun startSelectedAnalysis() {
        val selectedCandidate = _uiState.value.selectedCandidate ?: return
        if (selectedCandidate.state != AnalysisCandidateState.READY || _uiState.value.isStartingAnalysis) return

        viewModelScope.launch {
            _uiState.update { it.copy(isStartingAnalysis = true, errorMessage = null, completedAnalysis = null) }
            when (val result = startAnalysis(selectedCandidate.activityId)) {
                is AnalysisWorkflowResult.Success -> {
                    _uiState.update {
                        it.copy(isStartingAnalysis = false, activeAnalysis = result.data)
                    }
                    handleJobUpdate(result.data)
                }

                is AnalysisWorkflowResult.Failure -> {
                    val message = result.error.toUserMessage()
                    _uiState.update { it.copy(isStartingAnalysis = false, errorMessage = message) }
                    if (result.error is AnalysisWorkflowError.Conflict) {
                        refreshCandidates(errorMessage = message)
                    }
                }
            }
        }
    }

    fun loadCompletedAnalysis() {
        val analysisId = _uiState.value.selectedCandidate?.latestAnalysis?.analysisId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (val result = getAnalysis(analysisId)) {
                is AnalysisWorkflowResult.Success -> handleJobUpdate(result.data)
                is AnalysisWorkflowResult.Failure -> _uiState.update {
                    it.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    private fun refreshCandidates(errorMessage: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCandidates = true) }
            when (val result = getAnalysisCandidates()) {
                is AnalysisWorkflowResult.Success -> {
                    val candidates = result.data
                    val currentSelection = _uiState.value.selectedActivityId
                    val selectedActivityId = currentSelection.takeIf { selectedId ->
                        candidates.any { it.activityId == selectedId }
                    } ?: candidates.firstOrNull()?.activityId
                    _uiState.update {
                        it.copy(
                            isLoadingCandidates = false,
                            candidates = candidates,
                            selectedActivityId = selectedActivityId,
                            errorMessage = errorMessage,
                        )
                    }
                    startPollingForSelectedCandidate()
                }

                is AnalysisWorkflowResult.Failure -> _uiState.update {
                    it.copy(isLoadingCandidates = false, errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    private fun startPollingForSelectedCandidate() {
        val latestAnalysis = _uiState.value.selectedCandidate?.latestAnalysis ?: return
        if (latestAnalysis.status in setOf(AnalysisJobStatus.QUEUED, AnalysisJobStatus.PROCESSING)) {
            _uiState.update { it.copy(activeAnalysis = latestAnalysis) }
            startPolling(latestAnalysis.analysisId)
        }
    }

    private fun handleJobUpdate(job: AnalysisJob) {
        _uiState.update {
            when (job.status) {
                AnalysisJobStatus.COMPLETED -> it.copy(
                    activeAnalysis = null,
                    completedAnalysis = job,
                    errorMessage = null,
                )

                AnalysisJobStatus.FAILED -> it.copy(
                    activeAnalysis = null,
                    errorMessage = job.errorMessage ?: "Analysen kunne ikke fullføres.",
                )

                AnalysisJobStatus.QUEUED,
                AnalysisJobStatus.PROCESSING,
                -> it.copy(activeAnalysis = job)
            }
        }

        when (job.status) {
            AnalysisJobStatus.QUEUED,
            AnalysisJobStatus.PROCESSING,
            -> startPolling(job.analysisId)

            AnalysisJobStatus.COMPLETED,
            AnalysisJobStatus.FAILED,
            -> pollingJob?.cancel()
        }
    }

    private fun startPolling(analysisId: String) {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MILLIS)
                when (val result = getAnalysis(analysisId)) {
                    is AnalysisWorkflowResult.Success -> {
                        handleJobUpdate(result.data)
                        if (result.data.status in setOf(AnalysisJobStatus.COMPLETED, AnalysisJobStatus.FAILED)) {
                            refreshCandidates(errorMessage = _uiState.value.errorMessage)
                            return@launch
                        }
                    }

                    is AnalysisWorkflowResult.Failure -> {
                        _uiState.update { it.copy(errorMessage = result.error.toUserMessage()) }
                        return@launch
                    }
                }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    private fun AnalysisWorkflowError.toUserMessage(): String =
        when (this) {
            AnalysisWorkflowError.InvalidInput -> "Analysen kan ikke startes med dette grunnlaget."
            AnalysisWorkflowError.NetworkUnavailable -> "Ingen nettverkstilkobling. Prøv igjen."
            AnalysisWorkflowError.InvalidServerResponse -> "Kunne ikke lese svaret fra serveren."
            AnalysisWorkflowError.Unauthorized -> "Du har ikke tilgang til analysen."
            AnalysisWorkflowError.NotFound -> "Analysen ble ikke funnet."
            is AnalysisWorkflowError.Conflict -> message ?: "En analyse er allerede i gang."
            is AnalysisWorkflowError.ServerError -> "Serveren kunne ikke behandle analysen nå."
            is AnalysisWorkflowError.Unexpected -> message ?: "Noe gikk galt. Prøv igjen."
        }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 2_500L
    }
}
