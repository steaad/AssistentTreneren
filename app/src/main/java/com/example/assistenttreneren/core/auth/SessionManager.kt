package com.example.assistenttreneren.core.auth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val jwtDecoder: JwtDecoder,
    private val tokenRefresher: AuthTokenRefresher,
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _isBackendBypassActive = MutableStateFlow(false)
    val isBackendBypassActive: StateFlow<Boolean> = _isBackendBypassActive.asStateFlow()

    suspend fun initializeSession() {
        val accessToken = tokenStorage.getAccessToken()
        _isBackendBypassActive.value = false

        _sessionState.value = when {
            accessToken.isNullOrBlank() -> SessionState.Unauthenticated
            jwtDecoder.isExpired(accessToken) -> refreshExpiredSession()

            else -> SessionState.Authenticated
        }
    }

    private suspend fun refreshExpiredSession(): SessionState {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            tokenStorage.clearTokens()
            return SessionState.Unauthenticated
        }

        return if (tokenRefresher.refreshTokens() != null) {
            SessionState.Authenticated
        } else {
            tokenStorage.clearTokens()
            SessionState.SessionExpired
        }
    }

    fun onLoginSucceeded(isBackendBypass: Boolean = false) {
        _isBackendBypassActive.value = isBackendBypass
        _sessionState.value = SessionState.Authenticated
    }

    suspend fun logout() {
        tokenStorage.clearTokens()
        _isBackendBypassActive.value = false
        _sessionState.value = SessionState.Unauthenticated
    }

    suspend fun onSessionExpired() {
        tokenStorage.clearTokens()
        _isBackendBypassActive.value = false
        _sessionState.value = SessionState.SessionExpired
    }
}
