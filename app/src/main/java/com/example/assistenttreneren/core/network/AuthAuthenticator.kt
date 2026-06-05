package com.example.assistenttreneren.core.network

import com.example.assistenttreneren.core.auth.AuthTokenRefresher
import com.example.assistenttreneren.core.auth.SessionManager
import com.example.assistenttreneren.core.auth.TokenStorage
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val tokenRefresher: AuthTokenRefresher,
    private val sessionManager: SessionManager,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(AuthInterceptor.NO_AUTH_HEADER) != null) {
            return null
        }

        if (response.responseCount >= MAX_AUTH_ATTEMPTS) {
            expireSession()
            return null
        }

        val requestAccessToken = response.request.bearerToken()
        val storedAccessToken = runBlocking(Dispatchers.IO) {
            tokenStorage.getAccessToken()
        }?.trim()

        if (!storedAccessToken.isNullOrBlank() && storedAccessToken != requestAccessToken) {
            return response.request.withBearerToken(storedAccessToken)
        }

        val refreshedTokens = runBlocking(Dispatchers.IO) {
            tokenRefresher.refreshTokens()
        } ?: run {
            expireSession()
            return null
        }

        return response.request.withBearerToken(refreshedTokens.accessToken)
    }

    private fun expireSession() {
        runBlocking(Dispatchers.IO) {
            sessionManager.onSessionExpired()
        }
    }

    private fun Request.bearerToken(): String? {
        val authorization = header(AUTHORIZATION_HEADER)?.trim() ?: return null
        if (!authorization.startsWith(BEARER_PREFIX_WITH_SPACE, ignoreCase = true)) {
            return null
        }

        return authorization.substring(BEARER_PREFIX_WITH_SPACE.length).trim()
    }

    private fun Request.withBearerToken(accessToken: String): Request =
        newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX_WITH_SPACE$accessToken")
            .build()

    private val Response.responseCount: Int
        get() {
            var response: Response? = this
            var count = 1
            while (response?.priorResponse != null) {
                count++
                response = response.priorResponse
            }
            return count
        }

    private companion object {
        const val MAX_AUTH_ATTEMPTS = 2
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX_WITH_SPACE = "Bearer "
    }
}
