package com.example.assistenttreneren.feature.settings.data.repository

import com.example.assistenttreneren.feature.activitywizard.data.remote.LearningCatalogApi
import com.example.assistenttreneren.feature.activitywizard.data.remote.LearningCatalogItemDto
import com.example.assistenttreneren.feature.activitywizard.data.remote.UpsertLearningCatalogRequestDto
import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction
import com.example.assistenttreneren.feature.settings.domain.repository.CatalogResult
import com.example.assistenttreneren.feature.settings.domain.repository.LearningCatalogManagementRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import org.json.JSONObject

class LearningCatalogManagementRepositoryImpl @Inject constructor(
    private val api: LearningCatalogApi,
) : LearningCatalogManagementRepository {
    override suspend fun getTeamFunctions() = execute { api.getTeamFunctions().mapNotNull { runCatching { TeamFunction.valueOf(it) }.getOrNull() } }
    override suspend fun getThemes() = execute { api.getThemes().map { it.toDomain() } }
    override suspend fun getSubthemes(themeId: String) = execute { api.getSubthemes(themeId).map { it.toDomain() } }
    override suspend fun getObjectives(themeId: String) = execute { api.getLearningObjectives(themeId).map { it.toDomain() } }
    override suspend fun createTheme(name: String, description: String?, teamFunction: TeamFunction) = execute { api.createTheme(request(name, description, teamFunction = teamFunction.name)) }
    override suspend fun createSubtheme(themeId: String, name: String, description: String?) = execute { api.createSubtheme(themeId, request(name, description)) }
    override suspend fun createObjective(themeId: String, name: String, description: String?, subthemeId: String?) = execute { api.createLearningObjective(themeId, request(name, description, subthemeId = subthemeId)) }
    override suspend fun updateTheme(item: LearningCatalogItem, name: String, description: String?, teamFunction: TeamFunction) = execute { api.updateTheme(item.id, request(name, description, teamFunction = teamFunction.name)) }
    override suspend fun updateSubtheme(item: LearningCatalogItem, name: String, description: String?) = execute { api.updateSubtheme(item.id, request(name, description)) }
    override suspend fun updateObjective(item: LearningCatalogItem, name: String, description: String?) = execute { api.updateLearningObjective(item.id, request(name, description)) }
    override suspend fun deleteTheme(id: String) = execute { api.deleteTheme(id) }
    override suspend fun deleteSubtheme(id: String) = execute { api.deleteSubtheme(id) }
    override suspend fun deleteObjective(id: String) = execute { api.deleteLearningObjective(id) }

    private fun request(name: String, description: String?, teamFunction: String? = null, subthemeId: String? = null) = UpsertLearningCatalogRequestDto(name.trim(), description?.trim()?.ifBlank { null }, teamFunction, subthemeId)
    private suspend fun <T> execute(call: suspend () -> T): CatalogResult<T> = try { CatalogResult.Success(call()) } catch (e: CancellationException) { throw e } catch (e: HttpException) { CatalogResult.Failure(e.backendMessage()) } catch (_: IOException) { CatalogResult.Failure("Ingen nettverkstilkobling. Prøv igjen når du er på nett.") } catch (_: Exception) { CatalogResult.Failure("Kunne ikke fullføre endringen.") }
    private fun HttpException.backendMessage(): String = response()?.errorBody()?.string()?.let { body -> runCatching { JSONObject(body).optString("message").ifBlank { "Ugyldig endring." } }.getOrDefault("Ugyldig endring.") } ?: "Ugyldig endring."
    private fun LearningCatalogItemDto.toDomain() = LearningCatalogItem(id, name, description, parentId, teamFunction?.let { runCatching { TeamFunction.valueOf(it) }.getOrNull() })
}
