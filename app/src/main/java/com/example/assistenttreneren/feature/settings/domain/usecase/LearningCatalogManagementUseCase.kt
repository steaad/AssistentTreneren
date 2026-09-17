package com.example.assistenttreneren.feature.settings.domain.usecase

import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction
import com.example.assistenttreneren.feature.settings.domain.repository.LearningCatalogManagementRepository
import javax.inject.Inject

class LearningCatalogManagementUseCase @Inject constructor(private val repository: LearningCatalogManagementRepository) {
    suspend fun teamFunctions() = repository.getTeamFunctions()
    suspend fun themes() = repository.getThemes()
    suspend fun subthemes(themeId: String) = repository.getSubthemes(themeId)
    suspend fun objectives(themeId: String) = repository.getObjectives(themeId)
    suspend fun createTheme(name: String, description: String?, teamFunction: TeamFunction) = repository.createTheme(name, description, teamFunction)
    suspend fun createSubtheme(themeId: String, name: String, description: String?) = repository.createSubtheme(themeId, name, description)
    suspend fun createObjective(themeId: String, name: String, description: String?, subthemeId: String?) = repository.createObjective(themeId, name, description, subthemeId)
    suspend fun updateTheme(item: LearningCatalogItem, name: String, description: String?, teamFunction: TeamFunction) = repository.updateTheme(item, name, description, teamFunction)
    suspend fun updateSubtheme(item: LearningCatalogItem, name: String, description: String?) = repository.updateSubtheme(item, name, description)
    suspend fun updateObjective(item: LearningCatalogItem, name: String, description: String?) = repository.updateObjective(item, name, description)
    suspend fun deleteTheme(id: String) = repository.deleteTheme(id)
    suspend fun deleteSubtheme(id: String) = repository.deleteSubtheme(id)
    suspend fun deleteObjective(id: String) = repository.deleteObjective(id)
}
