package com.example.assistenttreneren.feature.login.data.repository

import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.core.auth.TokenStorage
import com.example.assistenttreneren.feature.login.data.dto.LoginRequestDto
import com.example.assistenttreneren.feature.login.data.mapper.toAuthTokens
import com.example.assistenttreneren.feature.login.data.remote.AuthApi
import com.example.assistenttreneren.feature.login.domain.repository.AuthError
import com.example.assistenttreneren.feature.login.domain.repository.AuthRepository
import com.example.assistenttreneren.feature.login.domain.repository.AuthResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): AuthResult<AuthTokens> = withContext(Dispatchers.IO) {
        try {
            val tokens = authApi.login(
                LoginRequestDto(
                    email = email.trim(),
                    password = password,
                ),
            ).toAuthTokens()

            if (!tokens.isValid()) {
                return@withContext AuthResult.Failure(AuthError.InvalidServerResponse)
            }

            tokenStorage.saveTokens(tokens)
            AuthResult.Success(tokens)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            AuthResult.Failure(exception.toAuthError())
        } catch (exception: IOException) {
            AuthResult.Failure(AuthError.NetworkUnavailable)
        } catch (exception: SerializationException) {
            AuthResult.Failure(AuthError.InvalidServerResponse)
        } catch (exception: IllegalArgumentException) {
            AuthResult.Failure(AuthError.InvalidServerResponse)
        } catch (exception: Exception) {
            AuthResult.Failure(AuthError.Unexpected(exception.message))
        }
    }

    private fun AuthTokens.isValid(): Boolean =
        accessToken.isNotBlank() &&
            refreshToken.isNotBlank() &&
            expiresIn > 0

    private fun HttpException.toAuthError(): AuthError =
        when (code()) {
            400, 401, 403 -> AuthError.InvalidCredentials
            in 500..599 -> AuthError.ServerError(code())
            else -> AuthError.Unexpected(message())
        }
}
