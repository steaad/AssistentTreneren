package com.example.assistenttreneren.feature.settings.domain.usecase

import com.example.assistenttreneren.feature.settings.domain.model.NewUser
import com.example.assistenttreneren.feature.settings.domain.repository.CreateUserResult
import com.example.assistenttreneren.feature.settings.domain.repository.UserManagementRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val repository: UserManagementRepository,
) {
    suspend operator fun invoke(user: NewUser): CreateUserResult = repository.createUser(user)
}
