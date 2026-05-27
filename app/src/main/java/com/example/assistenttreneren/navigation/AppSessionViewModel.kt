package com.example.assistenttreneren.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistenttreneren.core.auth.SessionManager
import com.example.assistenttreneren.core.auth.SessionState
import com.example.assistenttreneren.feature.login.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionManager.sessionState

    init {
        viewModelScope.launch {
            sessionManager.initializeSession()
        }
    }

    fun onLoginSucceeded() {
        sessionManager.onLoginSucceeded()
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
