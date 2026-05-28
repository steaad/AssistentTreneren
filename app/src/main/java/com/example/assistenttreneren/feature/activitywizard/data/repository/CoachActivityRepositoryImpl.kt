package com.example.assistenttreneren.feature.activitywizard.data.repository

import com.example.assistenttreneren.feature.activitywizard.data.dto.UpdateCoachActivityRequestDto
import com.example.assistenttreneren.feature.activitywizard.data.mapper.toCoachActivity
import com.example.assistenttreneren.feature.activitywizard.data.remote.CoachActivityApi
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityError
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class CoachActivityRepositoryImpl @Inject constructor(
    private val coachActivityApi: CoachActivityApi,
) : CoachActivityRepository {
    override suspend fun createActivity(): CoachActivityResult<CoachActivity> =
        withContext(Dispatchers.IO) {
            runRequest {
                coachActivityApi.createActivity().toCoachActivity()
            }
        }

    override suspend fun updateActivity(
        activityId: String,
        activityCategory: String?,
        title: String?,
    ): CoachActivityResult<CoachActivity> = withContext(Dispatchers.IO) {
        if (activityId.isBlank()) {
            return@withContext CoachActivityResult.Failure(CoachActivityError.InvalidInput)
        }

        runRequest {
            coachActivityApi.updateActivity(
                activityId = activityId,
                request = UpdateCoachActivityRequestDto(
                    activityCategory = activityCategory?.trim()?.ifBlank { null },
                    title = title?.trim()?.ifBlank { null },
                ),
            ).toCoachActivity()
        }
    }

    override suspend fun getActivities(): CoachActivityResult<List<CoachActivity>> =
        withContext(Dispatchers.IO) {
            runRequest {
                coachActivityApi.getActivities().map { it.toCoachActivity() }
            }
        }

    override suspend fun getActivity(
        activityId: String,
    ): CoachActivityResult<CoachActivity> = withContext(Dispatchers.IO) {
        if (activityId.isBlank()) {
            return@withContext CoachActivityResult.Failure(CoachActivityError.InvalidInput)
        }

        runRequest {
            coachActivityApi.getActivity(activityId).toCoachActivity()
        }
    }

    private suspend fun <T> runRequest(
        request: suspend () -> T,
    ): CoachActivityResult<T> =
        try {
            CoachActivityResult.Success(request())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            CoachActivityResult.Failure(exception.toCoachActivityError())
        } catch (exception: IOException) {
            CoachActivityResult.Failure(CoachActivityError.NetworkUnavailable)
        } catch (exception: SerializationException) {
            CoachActivityResult.Failure(CoachActivityError.InvalidServerResponse)
        } catch (exception: IllegalArgumentException) {
            CoachActivityResult.Failure(CoachActivityError.InvalidServerResponse)
        } catch (exception: Exception) {
            CoachActivityResult.Failure(CoachActivityError.Unexpected(exception.message))
        }

    private fun HttpException.toCoachActivityError(): CoachActivityError =
        when (code()) {
            400, 422 -> CoachActivityError.InvalidInput
            401, 403 -> CoachActivityError.Unauthorized
            404 -> CoachActivityError.NotFound
            in 500..599 -> CoachActivityError.ServerError(code())
            else -> CoachActivityError.Unexpected(message())
        }
}
