package com.example.assistenttreneren.feature.login.domain.repository

sealed interface AuthResult<out T> {
    data class Success<T>(
        val data: T,
    ) : AuthResult<T>

    data class Failure(
        val error: AuthError,
    ) : AuthResult<Nothing>
}

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object PasswordChangeRequired : AuthError
    data object PasswordMustDiffer : AuthError
    data object ValidationError : AuthError
    data object NetworkUnavailable : AuthError
    data object InvalidServerResponse : AuthError

    data class ServerError(
        val code: Int,
    ) : AuthError

    data class Unexpected(
        val message: String?,
    ) : AuthError
}
