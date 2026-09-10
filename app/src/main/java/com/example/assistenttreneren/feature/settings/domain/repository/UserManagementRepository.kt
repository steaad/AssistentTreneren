package com.example.assistenttreneren.feature.settings.domain.repository

import com.example.assistenttreneren.feature.settings.domain.model.NewUser

interface UserManagementRepository {
    suspend fun createUser(user: NewUser): CreateUserResult
}

sealed interface CreateUserResult {
    data object Success : CreateUserResult
    data object Unauthorized : CreateUserResult
    data object Forbidden : CreateUserResult
    data object EmailAlreadyExists : CreateUserResult
    data object ValidationError : CreateUserResult
    data object NetworkUnavailable : CreateUserResult
    data object UnexpectedError : CreateUserResult
}
