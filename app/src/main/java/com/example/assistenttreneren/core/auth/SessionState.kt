package com.example.assistenttreneren.core.auth

sealed interface SessionState {
    data object Loading : SessionState
    data object Authenticated : SessionState
    data object Unauthenticated : SessionState
    data object SessionExpired : SessionState
}
