package com.example.assistenttreneren.feature.analysis.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.feature.analysis.data.dto.MatchAnalysisResultDto
import com.example.assistenttreneren.feature.analysis.data.mapper.toMatchAnalysisResult
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJobStatus
import com.example.assistenttreneren.feature.analysis.domain.model.AnalysisJob
import com.example.assistenttreneren.feature.analysis.domain.model.MatchAnalysisResult
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowError
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowResult
import com.example.assistenttreneren.feature.analysis.domain.usecase.GetAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

data class AnalysisResultUiState(
    val isLoading: Boolean = true,
    val analysisMetadata: AnalysisJob? = null,
    val matchResult: MatchAnalysisResult? = null,
    val unsupportedResult: JsonElement? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class AnalysisResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAnalysis: GetAnalysisUseCase,
) : ViewModel() {
    private val analysisId: String = checkNotNull(savedStateHandle[ANALYSIS_ID_ARGUMENT])
    private val _uiState = MutableStateFlow(AnalysisResultUiState())
    val uiState: StateFlow<AnalysisResultUiState> = _uiState.asStateFlow()

    init {
        loadAnalysis()
    }

    fun retry() = loadAnalysis()

    private fun loadAnalysis() {
        viewModelScope.launch {
            _uiState.value = AnalysisResultUiState()
            when (val result = getAnalysis(analysisId)) {
                is AnalysisWorkflowResult.Success -> showAnalysis(result.data)
                is AnalysisWorkflowResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    private fun showAnalysis(analysis: AnalysisJob) {
        if (analysis.status != AnalysisJobStatus.COMPLETED || analysis.result == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    analysisMetadata = analysis,
                    errorMessage = analysis.errorMessage ?: "Analysen er ikke klar ennå.",
                )
            }
            return
        }

        if (!analysis.promptVersion.startsWith(MATCH_ANALYSIS_PROMPT_PREFIX)) {
            _uiState.update {
                it.copy(isLoading = false, analysisMetadata = analysis, unsupportedResult = analysis.result)
            }
            return
        }

        val matchResult = runCatching {
            json.decodeFromJsonElement<MatchAnalysisResultDto>(analysis.result).toMatchAnalysisResult()
        }.getOrNull()

        _uiState.update {
            if (matchResult == null) {
                it.copy(
                    isLoading = false,
                    errorMessage = "Kunne ikke vise resultatet fra analysen.",
                )
            } else {
                it.copy(isLoading = false, analysisMetadata = analysis, matchResult = matchResult)
            }
        }
    }

    private fun AnalysisWorkflowError.toUserMessage(): String =
        when (this) {
            AnalysisWorkflowError.NetworkUnavailable -> "Ingen nettverkstilkobling. Prøv igjen."
            AnalysisWorkflowError.NotFound -> "Analysen ble ikke funnet."
            AnalysisWorkflowError.Unauthorized -> "Du har ikke tilgang til analysen."
            else -> "Kunne ikke hente analysen. Prøv igjen."
        }

    private companion object {
        const val ANALYSIS_ID_ARGUMENT = "analysisId"
        const val MATCH_ANALYSIS_PROMPT_PREFIX = "match-analysis"
        val json = Json { ignoreUnknownKeys = true }
    }
}
