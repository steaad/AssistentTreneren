package com.example.assistenttreneren.feature.activitywizard.domain.usecase

import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import javax.inject.Inject

class GetCoachActivityUseCase @Inject constructor(
    private val coachActivityRepository: CoachActivityRepository,
) {
    suspend operator fun invoke(
        activityId: String,
    ) = coachActivityRepository.getActivity(activityId)
}
