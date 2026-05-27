package com.example.assistenttreneren.feature.login.domain.usecase

import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.feature.login.domain.repository.AuthRepository
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): AuthResult<AuthTokens> = authRepository.login(
        email = email,
        password = password,
    )
}
