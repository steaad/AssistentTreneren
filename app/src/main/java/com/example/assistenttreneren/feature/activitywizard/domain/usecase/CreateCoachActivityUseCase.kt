package com.example.assistenttreneren.feature.activitywizard.domain.usecase

import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import javax.inject.Inject

class CreateCoachActivityUseCase @Inject constructor(
    private val coachActivityRepository: CoachActivityRepository,
) {
    suspend operator fun invoke() = coachActivityRepository.createActivity()
}
