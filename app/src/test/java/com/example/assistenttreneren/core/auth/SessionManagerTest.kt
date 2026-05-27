package com.example.assistenttreneren.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class SessionManagerTest {
    @Test
    fun logoutClearsTokensAndResetsSessionState() = runBlocking {
        val tokenStorage = FakeTokenStorage()
        val sessionManager = SessionManager(
            tokenStorage = tokenStorage,
            jwtDecoder = JwtDecoder(),
        )

        sessionManager.onLoginSucceeded()
        sessionManager.logout()

        assertTrue(tokenStorage.clearTokensCalled)
        assertEquals(SessionState.Unauthenticated, sessionManager.sessionState.value)
    }

    private class FakeTokenStorage : TokenStorage {
        var clearTokensCalled = false

        override suspend fun saveTokens(tokens: AuthTokens) = Unit

        override suspend fun getAccessToken(): String? = null

        override suspend fun getRefreshToken(): String? = null

        override suspend fun clearTokens() {
            clearTokensCalled = true
        }
    }
}
