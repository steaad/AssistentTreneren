package com.example.assistenttreneren.feature.login.presentation

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val isInitialPasswordChangeRequired: Boolean = false,
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
    data object ValidationError : LoginError

    data class ServerError(
        val code: Int,
    ) : LoginError

    data object Unexpected : LoginError
}

data class InitialPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isPasswordChangeSuccessful: Boolean = false,
    val error: InitialPasswordError? = null,
)

sealed interface InitialPasswordError {
    data object MissingCredentials : InitialPasswordError
    data object PasswordTooShort : InitialPasswordError
    data object PasswordsDoNotMatch : InitialPasswordError
    data object PasswordMustDiffer : InitialPasswordError
    data object InvalidCredentials : InitialPasswordError
    data object ValidationError : InitialPasswordError
    data object NetworkUnavailable : InitialPasswordError
    data object Unexpected : InitialPasswordError
}
