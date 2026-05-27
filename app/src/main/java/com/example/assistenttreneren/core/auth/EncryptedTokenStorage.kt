package com.example.assistenttreneren.core.auth

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class EncryptedTokenStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
                .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
                .putLong(KEY_EXPIRES_IN, tokens.expiresIn)
                .apply()
        }
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            val isCleared = sharedPreferences.edit()
                .clear()
                .commit()

            check(isCleared) { "Failed to clear secure token storage." }
        }
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_IN = "expires_in"
    }
}
