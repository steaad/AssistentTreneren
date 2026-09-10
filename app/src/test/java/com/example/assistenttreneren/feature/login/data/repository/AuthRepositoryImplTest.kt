package com.example.assistenttreneren.feature.login.data.repository

import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.core.auth.TokenStorage
import com.example.assistenttreneren.feature.login.data.dto.LoginRequestDto
import com.example.assistenttreneren.feature.login.data.dto.ChangeInitialPasswordRequestDto
import com.example.assistenttreneren.feature.login.data.dto.LoginResponseDto
import com.example.assistenttreneren.feature.login.data.dto.RefreshTokenRequestDto
import com.example.assistenttreneren.feature.login.data.remote.AuthApi
import com.example.assistenttreneren.feature.login.domain.repository.AuthError
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class AuthRepositoryImplTest {
    @Test
    fun refreshTokensSavesAndReturnsNewTokens() = runBlocking {
        val tokenStorage = FakeTokenStorage(refreshToken = "old-refresh")
        val authApi = FakeAuthApi(
            refreshResponse = LoginResponseDto(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                expiresIn = 3600,
            ),
        )
        val repository = AuthRepositoryImpl(
            authApi = authApi,
            tokenStorage = tokenStorage,
        )

        val result = repository.refreshTokens()

        assertTrue(result is AuthResult.Success)
        assertEquals("old-refresh", authApi.refreshRequest?.refreshToken)
        assertEquals("new-access", tokenStorage.savedTokens?.accessToken)
        assertEquals("new-refresh", tokenStorage.savedTokens?.refreshToken)
    }

    @Test
    fun refreshTokensFailsWhenRefreshTokenIsMissing() = runBlocking {
        val repository = AuthRepositoryImpl(
            authApi = FakeAuthApi(),
            tokenStorage = FakeTokenStorage(refreshToken = null),
        )

        val result = repository.refreshTokens()

        assertEquals(AuthResult.Failure(AuthError.InvalidCredentials), result)
    }

    private class FakeAuthApi(
        private val refreshResponse: LoginResponseDto = LoginResponseDto(
            accessToken = "access",
            refreshToken = "refresh",
            expiresIn = 3600,
        ),
    ) : AuthApi {
        var refreshRequest: RefreshTokenRequestDto? = null

        override suspend fun login(request: LoginRequestDto): LoginResponseDto =
            LoginResponseDto(
                accessToken = "access",
                refreshToken = "refresh",
                expiresIn = 3600,
            )

        override suspend fun refresh(request: RefreshTokenRequestDto): LoginResponseDto {
            refreshRequest = request
            return refreshResponse
        }

        override suspend fun changeInitialPassword(
            request: ChangeInitialPasswordRequestDto,
        ): LoginResponseDto = LoginResponseDto(
            accessToken = "access",
            refreshToken = "refresh",
            expiresIn = 3600,
        )
    }

    private class FakeTokenStorage(
        private val refreshToken: String?,
    ) : TokenStorage {
        var savedTokens: AuthTokens? = null

        override suspend fun saveTokens(tokens: AuthTokens) {
            savedTokens = tokens
        }

        override suspend fun getAccessToken(): String? = savedTokens?.accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override suspend fun clearTokens() = Unit
    }
}
