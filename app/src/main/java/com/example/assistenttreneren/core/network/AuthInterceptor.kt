package com.example.assistenttreneren.core.network

import com.example.assistenttreneren.core.auth.TokenStorage
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.header(NO_AUTH_HEADER) != null) {
            return chain.proceed(
                request.newBuilder()
                    .removeHeader(NO_AUTH_HEADER)
                    .build(),
            )
        }

        if (request.header(AUTHORIZATION_HEADER) != null) {
            return chain.proceed(request)
        }

        val accessToken = runBlocking(Dispatchers.IO) {
            tokenStorage.getAccessToken()
        }?.trim()

        if (accessToken.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    companion object {
        const val NO_AUTH_HEADER = "No-Authentication"

        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer"
    }
}
