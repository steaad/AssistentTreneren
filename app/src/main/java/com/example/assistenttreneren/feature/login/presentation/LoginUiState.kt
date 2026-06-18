package com.example.assistenttreneren.feature.login.presentation

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val isBackendBypassLogin: Boolean = false,
    val emailError: LoginInputError? = null,
    val passwordError: LoginInputError? = null,
    val loginError: LoginError? = null,
) {
    val canSubmit: Boolean
        get() = !isLoading && email.isNotBlank() && password.isNotBlank()
}

enum class LoginInputError {
    Required,
    InvalidEmail,
    PasswordTooShort,
}

sealed interface LoginError {
    data object InvalidCredentials : LoginError
    data object NetworkUnavailable : LoginError
    data object InvalidServerResponse : LoginError

    data class ServerError(
        val code: Int,
    ) : LoginError

    data object Unexpected : LoginError
}
