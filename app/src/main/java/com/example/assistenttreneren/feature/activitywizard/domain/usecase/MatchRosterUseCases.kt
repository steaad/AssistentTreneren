package com.example.assistenttreneren.feature.activitywizard.domain.usecase

import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import javax.inject.Inject

class GetMatchRosterUseCase @Inject constructor(private val repository: CoachActivityRepository) {
    suspend operator fun invoke(activityId: String) = repository.getMatchRoster(activityId)
}

class GetMatchRosterSuggestionsUseCase @Inject constructor(private val repository: CoachActivityRepository) {
    suspend operator fun invoke() = repository.getMatchRosterSuggestions()
}

class UpdateMatchRosterUseCase @Inject constructor(private val repository: CoachActivityRepository) {
    suspend operator fun invoke(activityId: String, playerNames: List<String>) = repository.updateMatchRoster(activityId, playerNames)
}
