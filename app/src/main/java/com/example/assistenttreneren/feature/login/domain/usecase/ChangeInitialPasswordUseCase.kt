package com.example.assistenttreneren.feature.login.domain.usecase

import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.feature.login.domain.repository.AuthRepository
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import javax.inject.Inject

class ChangeInitialPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        temporaryPassword: String,
        newPassword: String,
    ): AuthResult<AuthTokens> = authRepository.changeInitialPassword(
        email = email,
        temporaryPassword = temporaryPassword,
        newPassword = newPassword,
    )
}
