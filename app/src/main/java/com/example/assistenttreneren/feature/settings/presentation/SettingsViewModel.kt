package com.example.assistenttreneren.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.core.auth.JwtDecoder
import com.example.assistenttreneren.core.auth.TokenStorage
import com.example.assistenttreneren.core.auth.UserRole
import com.example.assistenttreneren.feature.settings.domain.model.NewUser
import com.example.assistenttreneren.feature.settings.domain.repository.CreateUserResult
import com.example.assistenttreneren.feature.settings.domain.usecase.CreateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isAdministrator: Boolean = false,
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.TRAINER,
    val temporaryPassword: String = "",
    val confirmTemporaryPassword: String = "",
    val isSubmitting: Boolean = false,
    val message: SettingsMessage? = null,
)

sealed interface SettingsMessage {
    data object Created : SettingsMessage
    data object ValidationError : SettingsMessage
    data object PasswordsDoNotMatch : SettingsMessage
    data object PasswordTooShort : SettingsMessage
    data object EmailAlreadyExists : SettingsMessage
    data object NetworkUnavailable : SettingsMessage
    data object Forbidden : SettingsMessage
    data object Unexpected : SettingsMessage
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val jwtDecoder: JwtDecoder,
    private val createUserUseCase: CreateUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val role = tokenStorage.getAccessToken()?.let(jwtDecoder::getRole)
            _uiState.update { it.copy(isAdministrator = role == UserRole.ADMINISTRATOR) }
        }
    }

    fun onEmailChanged(value: String) = updateForm { copy(email = value) }
    fun onDisplayNameChanged(value: String) = updateForm { copy(displayName = value) }
    fun onRoleChanged(value: UserRole) = updateForm { copy(role = value) }
    fun onTemporaryPasswordChanged(value: String) = updateForm { copy(temporaryPassword = value) }
    fun onConfirmTemporaryPasswordChanged(value: String) = updateForm { copy(confirmTemporaryPassword = value) }

    fun createUser(onUnauthorized: () -> Unit) {
        val state = _uiState.value
        val localMessage = when {
            !state.isAdministrator -> SettingsMessage.Forbidden
            state.email.isBlank() || state.displayName.isBlank() -> SettingsMessage.ValidationError
            state.temporaryPassword.length < MIN_PASSWORD_LENGTH -> SettingsMessage.PasswordTooShort
            state.temporaryPassword != state.confirmTemporaryPassword -> SettingsMessage.PasswordsDoNotMatch
            else -> null
        }
        if (state.isSubmitting || localMessage != null) {
            if (localMessage != null) _uiState.update { it.copy(message = localMessage) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }
            when (createUserUseCase(NewUser(state.email, state.displayName, state.role, state.temporaryPassword))) {
                CreateUserResult.Success -> _uiState.value = SettingsUiState(isAdministrator = true, message = SettingsMessage.Created)
                CreateUserResult.Unauthorized -> onUnauthorized()
                CreateUserResult.Forbidden -> _uiState.update { it.copy(isSubmitting = false, isAdministrator = false, message = SettingsMessage.Forbidden) }
                CreateUserResult.EmailAlreadyExists -> _uiState.update { it.copy(isSubmitting = false, message = SettingsMessage.EmailAlreadyExists) }
                CreateUserResult.ValidationError -> _uiState.update { it.copy(isSubmitting = false, message = SettingsMessage.ValidationError) }
                CreateUserResult.NetworkUnavailable -> _uiState.update { it.copy(isSubmitting = false, message = SettingsMessage.NetworkUnavailable) }
                CreateUserResult.UnexpectedError -> _uiState.update { it.copy(isSubmitting = false, message = SettingsMessage.Unexpected) }
            }
        }
    }

    private fun updateForm(transform: SettingsUiState.() -> SettingsUiState) {
        _uiState.update { it.transform().copy(message = null) }
    }

    private companion object { const val MIN_PASSWORD_LENGTH = 12 }
}
