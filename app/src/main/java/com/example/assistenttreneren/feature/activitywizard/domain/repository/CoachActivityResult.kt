package com.example.assistenttreneren.feature.activitywizard.domain.repository

sealed interface CoachActivityResult<out T> {
    data class Success<T>(
        val data: T,
    ) : CoachActivityResult<T>

    data class Failure(
        val error: CoachActivityError,
    ) : CoachActivityResult<Nothing>
}

sealed interface CoachActivityError {
    data object InvalidInput : CoachActivityError
    data object InvalidServerResponse : CoachActivityError
    data object NetworkUnavailable : CoachActivityError
    data object NotFound : CoachActivityError
    data object Unauthorized : CoachActivityError
    data class ServerError(
        val code: Int,
    ) : CoachActivityError

    data class Unexpected(
        val message: String?,
    ) : CoachActivityError
}
