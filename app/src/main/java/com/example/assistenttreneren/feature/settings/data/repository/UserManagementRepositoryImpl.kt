package com.example.assistenttreneren.feature.settings.data.repository

import com.example.assistenttreneren.feature.settings.data.dto.CreateUserRequestDto
import com.example.assistenttreneren.feature.settings.data.remote.UserManagementApi
import com.example.assistenttreneren.feature.settings.domain.model.NewUser
import com.example.assistenttreneren.feature.settings.domain.repository.CreateUserResult
import com.example.assistenttreneren.feature.settings.domain.repository.UserManagementRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class UserManagementRepositoryImpl @Inject constructor(
    private val api: UserManagementApi,
) : UserManagementRepository {
    override suspend fun createUser(user: NewUser): CreateUserResult = try {
        api.createUser(
            CreateUserRequestDto(
                email = user.email.trim(),
                displayName = user.displayName.trim(),
                role = user.role.name,
                temporaryPassword = user.temporaryPassword,
            ),
        )
        CreateUserResult.Success
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: HttpException) {
        when (exception.code()) {
            400 -> CreateUserResult.ValidationError
            401 -> CreateUserResult.Unauthorized
            403 -> CreateUserResult.Forbidden
            409 -> CreateUserResult.EmailAlreadyExists
            else -> CreateUserResult.UnexpectedError
        }
    } catch (_: IOException) {
        CreateUserResult.NetworkUnavailable
    } catch (_: Exception) {
        CreateUserResult.UnexpectedError
    }
}
