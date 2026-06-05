package com.example.assistenttreneren.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.coroutines.runBlocking

class SessionManagerTest {
    @Test
    fun logoutClearsTokensAndResetsSessionState() = runBlocking {
        val tokenStorage = FakeTokenStorage()
        val sessionManager = SessionManager(
            tokenStorage = tokenStorage,
            jwtDecoder = JwtDecoder(),
            tokenRefresher = FakeTokenRefresher(),
        )

        sessionManager.onLoginSucceeded()
        sessionManager.logout()

        assertTrue(tokenStorage.clearTokensCalled)
        assertEquals(SessionState.Unauthenticated, sessionManager.sessionState.value)
    }

    @Test
    fun initializeSessionAuthenticatesWhenAccessTokenIsValid() = runBlocking {
        val tokenStorage = FakeTokenStorage(
            accessToken = jwtWithExpiration(expiresAtSeconds = futureEpochSeconds()),
        )
        val sessionManager = SessionManager(
            tokenStorage = tokenStorage,
            jwtDecoder = JwtDecoder(),
            tokenRefresher = FakeTokenRefresher(),
        )

        sessionManager.initializeSession()

        assertEquals(SessionState.Authenticated, sessionManager.sessionState.value)
    }

    @Test
    fun initializeSessionRefreshesExpiredAccessTokenWhenRefreshTokenExists() = runBlocking {
        val tokenStorage = FakeTokenStorage(
            accessToken = jwtWithExpiration(expiresAtSeconds = pastEpochSeconds()),
            refreshToken = "refresh-token",
        )
        val tokenRefresher = FakeTokenRefresher(
            refreshedTokens = AuthTokens(
                accessToken = jwtWithExpiration(expiresAtSeconds = futureEpochSeconds()),
                refreshToken = "new-refresh-token",
                expiresIn = 3600,
            ),
        )
        val sessionManager = SessionManager(
            tokenStorage = tokenStorage,
            jwtDecoder = JwtDecoder(),
            tokenRefresher = tokenRefresher,
        )

        sessionManager.initializeSession()

        assertTrue(tokenRefresher.refreshTokensCalled)
        assertEquals(SessionState.Authenticated, sessionManager.sessionState.value)
    }

    @Test
    fun initializeSessionClearsTokensWhenExpiredAccessTokenCannotRefresh() = runBlocking {
        val tokenStorage = FakeTokenStorage(
            accessToken = jwtWithExpiration(expiresAtSeconds = pastEpochSeconds()),
            refreshToken = "refresh-token",
        )
        val sessionManager = SessionManager(
            tokenStorage = tokenStorage,
            jwtDecoder = JwtDecoder(),
            tokenRefresher = FakeTokenRefresher(refreshedTokens = null),
        )

        sessionManager.initializeSession()

        assertTrue(tokenStorage.clearTokensCalled)
        assertEquals(SessionState.SessionExpired, sessionManager.sessionState.value)
    }

    private class FakeTokenStorage : TokenStorage {
        constructor(
            accessToken: String? = null,
            refreshToken: String? = null,
        ) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
        }

        var clearTokensCalled = false
        private var accessToken: String? = null
        private var refreshToken: String? = null

        override suspend fun saveTokens(tokens: AuthTokens) {
            accessToken = tokens.accessToken
            refreshToken = tokens.refreshToken
        }

        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override suspend fun clearTokens() {
            clearTokensCalled = true
            accessToken = null
            refreshToken = null
        }
    }

    private class FakeTokenRefresher(
        private val refreshedTokens: AuthTokens? = null,
    ) : AuthTokenRefresher {
        var refreshTokensCalled = false

        override suspend fun refreshTokens(): AuthTokens? {
            refreshTokensCalled = true
            return refreshedTokens
        }
    }

    private fun jwtWithExpiration(expiresAtSeconds: Long): String {
        val header = """{"alg":"none"}""".base64Url()
        val payload = """{"exp":$expiresAtSeconds}""".base64Url()
        return "$header.$payload.signature"
    }

    private fun String.base64Url(): String =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(toByteArray(StandardCharsets.UTF_8))

    private fun futureEpochSeconds(): Long =
        (System.currentTimeMillis() / 1_000L) + 3_600L

    private fun pastEpochSeconds(): Long =
        (System.currentTimeMillis() / 1_000L) - 3_600L
}
