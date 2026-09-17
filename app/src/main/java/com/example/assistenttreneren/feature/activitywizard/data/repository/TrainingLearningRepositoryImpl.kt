package com.example.assistenttreneren.feature.activitywizard.data.repository

import com.example.assistenttreneren.feature.activitywizard.data.remote.LearningCatalogApi
import com.example.assistenttreneren.feature.activitywizard.data.remote.LearningCatalogItemDto
import com.example.assistenttreneren.feature.activitywizard.data.remote.TrainingLearningConfigRequestDto
import com.example.assistenttreneren.feature.activitywizard.data.remote.TrainingLearningConfigResponseDto
import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction
import com.example.assistenttreneren.feature.activitywizard.domain.model.TrainingLearningConfig
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityError
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityResult
import com.example.assistenttreneren.feature.activitywizard.domain.repository.TrainingLearningRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class TrainingLearningRepositoryImpl @Inject constructor(
    private val api: LearningCatalogApi,
) : TrainingLearningRepository {
    override suspend fun getTeamFunctions() = request { api.getTeamFunctions().mapNotNull { value -> runCatching { TeamFunction.valueOf(value) }.getOrNull() } }
    override suspend fun getThemes() = request { api.getThemes().map { it.toDomain() } }
    override suspend fun getSubthemes(themeId: String) = request { api.getSubthemes(themeId).map { it.toDomain() } }
    override suspend fun getLearningObjectives(themeId: String) = request { api.getLearningObjectives(themeId).map { it.toDomain() } }
    override suspend fun getTrainingLearningConfig(activityId: String) = request {
        api.getTrainingLearningConfig(activityId).toDomain()
    }
    override suspend fun updateTrainingLearningConfig(activityId: String, config: TrainingLearningConfig): CoachActivityResult<Unit> {
        val function = config.teamFunction ?: return CoachActivityResult.Failure(CoachActivityError.InvalidInput)
        val theme = config.theme ?: return CoachActivityResult.Failure(CoachActivityError.InvalidInput)
        if (config.objectives.isEmpty()) return CoachActivityResult.Failure(CoachActivityError.InvalidInput)
        return request {
            api.updateTrainingLearningConfig(activityId, TrainingLearningConfigRequestDto(function.name, theme.id, config.subtheme?.id, config.objectives.map { it.id }))
        }
    }

    private suspend fun <T> request(block: suspend () -> T): CoachActivityResult<T> = withContext(Dispatchers.IO) {
        try { CoachActivityResult.Success(block()) }
        catch (exception: CancellationException) { throw exception }
        catch (_: IOException) { CoachActivityResult.Failure(CoachActivityError.NetworkUnavailable) }
        catch (exception: HttpException) { CoachActivityResult.Failure(exception.toCoachActivityError()) }
        catch (exception: Exception) { CoachActivityResult.Failure(CoachActivityError.Unexpected(exception.message)) }
    }

    private fun HttpException.toCoachActivityError(): CoachActivityError =
        when (code()) {
            400, 409, 422 -> CoachActivityError.InvalidInput
            401, 403 -> CoachActivityError.Unauthorized
            404 -> CoachActivityError.NotFound
            in 500..599 -> CoachActivityError.ServerError(code())
            else -> CoachActivityError.Unexpected(message())
        }

    private fun LearningCatalogItemDto.toDomain() = LearningCatalogItem(id, name, description, parentId, teamFunction?.let { value -> runCatching { TeamFunction.valueOf(value) }.getOrNull() })

    private fun TrainingLearningConfigResponseDto.toDomain() = TrainingLearningConfig(
        teamFunction = teamFunction?.let { value -> runCatching { TeamFunction.valueOf(value) }.getOrNull() },
        theme = theme?.toDomain(),
        subtheme = subtheme?.toDomain(),
        objectives = objectives.map { it.toDomain() },
    )
}
