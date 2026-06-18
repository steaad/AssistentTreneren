package com.example.assistenttreneren.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.BuildConfig
import com.example.assistenttreneren.feature.login.domain.repository.AuthError
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import com.example.assistenttreneren.feature.login.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = null,
                loginError = null,
                isLoginSuccessful = false,
                isBackendBypassLogin = false,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = null,
                loginError = null,
                isLoginSuccessful = false,
                isBackendBypassLogin = false,
            )
        }
    }

    fun login() {
        val currentState = _uiState.value

        if (currentState.isLoading) {
            return
        }

        val validationResult = validateCredentials(
            email = currentState.email,
            password = currentState.password,
        )

        if (!validationResult.isValid) {
            _uiState.update {
                it.copy(
                    emailError = validationResult.emailError,
                    passwordError = validationResult.passwordError,
                    loginError = null,
                    isLoginSuccessful = false,
                    isBackendBypassLogin = false,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    loginError = null,
                    isLoginSuccessful = false,
                    isBackendBypassLogin = false,
                )
            }

            when (
                val result = loginUseCase(
                    email = currentState.email,
                    password = currentState.password,
                )
            ) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            isBackendBypassLogin = false,
                            loginError = null,
                        )
                    }
                }

                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = false,
                            isBackendBypassLogin = false,
                            loginError = result.error.toLoginError(),
                        )
                    }
                }
            }
        }
    }

    fun continueWithoutBackend() {
        if (!BuildConfig.DEBUG || _uiState.value.isLoading) {
            return
        }

        _uiState.update {
            it.copy(
                isLoginSuccessful = true,
                isBackendBypassLogin = true,
                loginError = null,
                emailError = null,
                passwordError = null,
            )
        }
    }

    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun consumeLoginSuccess() {
        _uiState.update {
            it.copy(
                isLoginSuccessful = false,
                isBackendBypassLogin = false,
            )
        }
    }

    private fun validateCredentials(
        email: String,
        password: String,
    ): CredentialValidationResult {
        val trimmedEmail = email.trim()
        val emailError = when {
            trimmedEmail.isBlank() -> LoginInputError.Required
            !trimmedEmail.isValidEmail() -> LoginInputError.InvalidEmail
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> LoginInputError.Required
            password.length < MIN_PASSWORD_LENGTH -> LoginInputError.PasswordTooShort
            else -> null
        }

        return CredentialValidationResult(
            emailError = emailError,
            passwordError = passwordError,
        )
    }

    private fun String.isValidEmail(): Boolean =
        EMAIL_REGEX.matches(this)

    private fun AuthError.toLoginError(): LoginError =
        when (this) {
            AuthError.InvalidCredentials -> LoginError.InvalidCredentials
            AuthError.NetworkUnavailable -> LoginError.NetworkUnavailable
            AuthError.InvalidServerResponse -> LoginError.InvalidServerResponse
            is AuthError.ServerError -> LoginError.ServerError(code)
            is AuthError.Unexpected -> LoginError.Unexpected
        }

    private data class CredentialValidationResult(
        val emailError: LoginInputError?,
        val passwordError: LoginInputError?,
    ) {
        val isValid: Boolean
            get() = emailError == null && passwordError == null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}
