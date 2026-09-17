package com.example.assistenttreneren.feature.activitywizard.domain.usecase

import com.example.assistenttreneren.feature.activitywizard.domain.model.TrainingLearningConfig
import com.example.assistenttreneren.feature.activitywizard.domain.repository.TrainingLearningRepository
import javax.inject.Inject

class GetTrainingLearningCatalogUseCase @Inject constructor(private val repository: TrainingLearningRepository) {
    suspend fun teamFunctions() = repository.getTeamFunctions()
    suspend fun themes() = repository.getThemes()
    suspend fun subthemes(themeId: String) = repository.getSubthemes(themeId)
    suspend fun objectives(themeId: String) = repository.getLearningObjectives(themeId)
}

class GetTrainingLearningConfigUseCase @Inject constructor(private val repository: TrainingLearningRepository) {
    suspend operator fun invoke(activityId: String) = repository.getTrainingLearningConfig(activityId)
}

class UpdateTrainingLearningConfigUseCase @Inject constructor(private val repository: TrainingLearningRepository) {
    suspend operator fun invoke(activityId: String, config: TrainingLearningConfig) = repository.updateTrainingLearningConfig(activityId, config)
}
