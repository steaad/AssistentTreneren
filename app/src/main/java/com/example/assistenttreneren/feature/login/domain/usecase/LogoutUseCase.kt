package com.example.assistenttreneren.feature.login.domain.usecase

import com.example.assistenttreneren.core.auth.SessionManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val sessionManager: SessionManager,
) {
    suspend operator fun invoke() {
        sessionManager.logout()
    }
}
