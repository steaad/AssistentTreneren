package com.example.assistenttreneren.feature.settings.domain.repository

import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction

interface LearningCatalogManagementRepository {
    suspend fun getTeamFunctions(): CatalogResult<List<TeamFunction>>
    suspend fun getThemes(): CatalogResult<List<LearningCatalogItem>>
    suspend fun getSubthemes(themeId: String): CatalogResult<List<LearningCatalogItem>>
    suspend fun getObjectives(themeId: String): CatalogResult<List<LearningCatalogItem>>
    suspend fun createTheme(name: String, description: String?, teamFunction: TeamFunction): CatalogResult<Unit>
    suspend fun createSubtheme(themeId: String, name: String, description: String?): CatalogResult<Unit>
    suspend fun createObjective(themeId: String, name: String, description: String?, subthemeId: String?): CatalogResult<Unit>
    suspend fun updateTheme(item: LearningCatalogItem, name: String, description: String?, teamFunction: TeamFunction): CatalogResult<Unit>
    suspend fun updateSubtheme(item: LearningCatalogItem, name: String, description: String?): CatalogResult<Unit>
    suspend fun updateObjective(item: LearningCatalogItem, name: String, description: String?): CatalogResult<Unit>
    suspend fun deleteTheme(id: String): CatalogResult<Unit>
    suspend fun deleteSubtheme(id: String): CatalogResult<Unit>
    suspend fun deleteObjective(id: String): CatalogResult<Unit>
}

sealed interface CatalogResult<out T> {
    data class Success<T>(val data: T) : CatalogResult<T>
    data class Failure(val message: String) : CatalogResult<Nothing>
}
