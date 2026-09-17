package com.example.assistenttreneren.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.core.auth.JwtDecoder
import com.example.assistenttreneren.core.auth.TokenStorage
import com.example.assistenttreneren.core.auth.UserRole
import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction
import com.example.assistenttreneren.feature.settings.domain.repository.CatalogResult
import com.example.assistenttreneren.feature.settings.domain.usecase.LearningCatalogManagementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearningCatalogUiState(
    val isAdministrator: Boolean = false,
    val teamFunctions: List<TeamFunction> = emptyList(),
    val themes: List<LearningCatalogItem> = emptyList(),
    val selectedTeamFunction: TeamFunction? = null,
    val selectedTheme: LearningCatalogItem? = null,
    val subthemes: List<LearningCatalogItem> = emptyList(),
    val objectives: List<LearningCatalogItem> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class LearningCatalogViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val jwtDecoder: JwtDecoder,
    private val catalog: LearningCatalogManagementUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LearningCatalogUiState())
    val uiState: StateFlow<LearningCatalogUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { if (tokenStorage.getAccessToken()?.let(jwtDecoder::getRole) == UserRole.ADMINISTRATOR) { _uiState.update { it.copy(isAdministrator = true) }; loadCatalog() } } }

    fun selectTeamFunction(function: TeamFunction?) { _uiState.update { it.copy(selectedTeamFunction = function, selectedTheme = null, subthemes = emptyList(), objectives = emptyList(), message = null) } }
    fun selectTheme(theme: LearningCatalogItem) { _uiState.update { it.copy(selectedTheme = theme, subthemes = emptyList(), objectives = emptyList(), message = null) }; loadThemeChildren(theme.id) }

    fun createTheme(name: String, description: String) { val function = uiState.value.selectedTeamFunction ?: return; mutate { catalog.createTheme(name, description, function) } }
    fun createSubtheme(name: String, description: String) { val theme = uiState.value.selectedTheme ?: return; mutate { catalog.createSubtheme(theme.id, name, description) } }
    fun createObjective(name: String, description: String, subtheme: LearningCatalogItem?) { val theme = uiState.value.selectedTheme ?: return; mutate { catalog.createObjective(theme.id, name, description, subtheme?.id) } }
    fun updateTheme(item: LearningCatalogItem, name: String, description: String) { val function = item.teamFunction ?: return; mutate { catalog.updateTheme(item, name, description, function) } }
    fun updateSubtheme(item: LearningCatalogItem, name: String, description: String) = mutate { catalog.updateSubtheme(item, name, description) }
    fun updateObjective(item: LearningCatalogItem, name: String, description: String) = mutate { catalog.updateObjective(item, name, description) }
    fun deleteTheme(item: LearningCatalogItem) { _uiState.update { it.copy(selectedTheme = null, subthemes = emptyList(), objectives = emptyList()) }; mutate { catalog.deleteTheme(item.id) } }
    fun deleteSubtheme(item: LearningCatalogItem) = mutate { catalog.deleteSubtheme(item.id) }
    fun deleteObjective(item: LearningCatalogItem) = mutate { catalog.deleteObjective(item.id) }

    private fun loadCatalog() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, message = null) }
        val functions = catalog.teamFunctions(); val themes = catalog.themes()
        _uiState.update { state -> if (functions is CatalogResult.Success && themes is CatalogResult.Success) state.copy(teamFunctions = functions.data, themes = themes.data, isLoading = false) else state.copy(isLoading = false, message = "Kunne ikke hente læringskatalogen.") }
    }

    private fun loadThemeChildren(themeId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val subthemes = catalog.subthemes(themeId); val objectives = catalog.objectives(themeId)
        _uiState.update { state -> if (subthemes is CatalogResult.Success && objectives is CatalogResult.Success) state.copy(subthemes = subthemes.data, objectives = objectives.data, isLoading = false) else state.copy(isLoading = false, message = "Kunne ikke hente underliggende katalogverdier.") }
    }

    private fun mutate(operation: suspend () -> CatalogResult<Unit>) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, message = null) }
        when (val result = operation()) {
            is CatalogResult.Success -> { val themeId = uiState.value.selectedTheme?.id; if (themeId == null) loadCatalog() else loadThemeChildren(themeId) }
            is CatalogResult.Failure -> _uiState.update { it.copy(isLoading = false, message = result.message) }
        }
    }
}
