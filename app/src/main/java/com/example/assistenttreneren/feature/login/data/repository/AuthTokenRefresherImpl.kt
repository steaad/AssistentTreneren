package com.example.assistenttreneren.feature.login.data.repository

import com.example.assistenttreneren.core.auth.AuthTokenRefresher
import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.feature.login.domain.repository.AuthRepository
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class AuthTokenRefresherImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : AuthTokenRefresher {
    private val mutex = Mutex()

    override suspend fun refreshTokens(): AuthTokens? =
        mutex.withLock {
            when (val result = authRepository.refreshTokens()) {
                is AuthResult.Success -> result.data
                is AuthResult.Failure -> null
            }
        }
}
