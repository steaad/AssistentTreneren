package com.example.assistenttreneren.feature.activitywizard.domain.usecase

import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import javax.inject.Inject

class UpdateCoachActivityUseCase @Inject constructor(
    private val coachActivityRepository: CoachActivityRepository,
) {
    suspend operator fun invoke(
        activityId: String,
        activityCategory: String?,
        title: String?,
    ) = coachActivityRepository.updateActivity(
        activityId = activityId,
        activityCategory = activityCategory,
        title = title,
    )
}
