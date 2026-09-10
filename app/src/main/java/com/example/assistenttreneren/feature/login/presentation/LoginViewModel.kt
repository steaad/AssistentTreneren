package com.example.assistenttreneren.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.BuildConfig
import com.example.assistenttreneren.feature.login.domain.repository.AuthError
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import com.example.assistenttreneren.feature.login.domain.usecase.LoginUseCase
import com.example.assistenttreneren.feature.login.domain.usecase.ChangeInitialPasswordUseCase
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
    private val changeInitialPasswordUseCase: ChangeInitialPasswordUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _initialPasswordUiState = MutableStateFlow(InitialPasswordUiState())
    val initialPasswordUiState: StateFlow<InitialPasswordUiState> = _initialPasswordUiState.asStateFlow()
    private var pendingInitialPasswordCredentials: InitialPasswordCredentials? = null

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
                    if (result.error == AuthError.PasswordChangeRequired) {
                        pendingInitialPasswordCredentials = InitialPasswordCredentials(
                            email = currentState.email.trim(),
                            temporaryPassword = currentState.password,
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isInitialPasswordChangeRequired = true,
                                loginError = null,
                            )
                        }
                        return@launch
                    }
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

    fun consumeInitialPasswordChangeRequired() {
        _uiState.update { it.copy(isInitialPasswordChangeRequired = false) }
    }

    fun onNewPasswordChanged(password: String) {
        _initialPasswordUiState.update { it.copy(newPassword = password, error = null, isPasswordChangeSuccessful = false) }
    }

    fun onConfirmNewPasswordChanged(password: String) {
        _initialPasswordUiState.update { it.copy(confirmPassword = password, error = null, isPasswordChangeSuccessful = false) }
    }

    fun changeInitialPassword() {
        val credentials = pendingInitialPasswordCredentials
        val state = _initialPasswordUiState.value
        val error = when {
            credentials == null -> InitialPasswordError.MissingCredentials
            state.newPassword.length < INITIAL_PASSWORD_MIN_LENGTH -> InitialPasswordError.PasswordTooShort
            state.newPassword != state.confirmPassword -> InitialPasswordError.PasswordsDoNotMatch
            state.newPassword == credentials.temporaryPassword -> InitialPasswordError.PasswordMustDiffer
            else -> null
        }
        if (error != null || state.isLoading) {
            if (error != null) _initialPasswordUiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _initialPasswordUiState.update { it.copy(isLoading = true, error = null) }
            when (val result = changeInitialPasswordUseCase(
                email = credentials!!.email,
                temporaryPassword = credentials.temporaryPassword,
                newPassword = state.newPassword,
            )) {
                is AuthResult.Success -> {
                    pendingInitialPasswordCredentials = null
                    _initialPasswordUiState.update { it.copy(isLoading = false, isPasswordChangeSuccessful = true) }
                }
                is AuthResult.Failure -> _initialPasswordUiState.update {
                    it.copy(isLoading = false, error = result.error.toInitialPasswordError())
                }
            }
        }
    }

    fun consumeInitialPasswordChangeSuccess() {
        _initialPasswordUiState.update { InitialPasswordUiState() }
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
            AuthError.PasswordChangeRequired -> LoginError.InvalidServerResponse
            AuthError.PasswordMustDiffer,
            AuthError.ValidationError,
            -> LoginError.ValidationError
            AuthError.NetworkUnavailable -> LoginError.NetworkUnavailable
            AuthError.InvalidServerResponse -> LoginError.InvalidServerResponse
            is AuthError.ServerError -> LoginError.ServerError(code)
            is AuthError.Unexpected -> LoginError.Unexpected
        }

    private fun AuthError.toInitialPasswordError(): InitialPasswordError =
        when (this) {
            AuthError.InvalidCredentials -> InitialPasswordError.InvalidCredentials
            AuthError.PasswordMustDiffer -> InitialPasswordError.PasswordMustDiffer
            AuthError.ValidationError -> InitialPasswordError.ValidationError
            AuthError.NetworkUnavailable -> InitialPasswordError.NetworkUnavailable
            else -> InitialPasswordError.Unexpected
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
        const val INITIAL_PASSWORD_MIN_LENGTH = 12
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }

    private data class InitialPasswordCredentials(
        val email: String,
        val temporaryPassword: String,
    )
}
