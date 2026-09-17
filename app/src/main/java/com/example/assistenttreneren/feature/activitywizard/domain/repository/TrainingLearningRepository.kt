package com.example.assistenttreneren.feature.activitywizard.domain.repository

import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TrainingLearningConfig
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction

interface TrainingLearningRepository {
    suspend fun getTeamFunctions(): CoachActivityResult<List<TeamFunction>>
    suspend fun getThemes(): CoachActivityResult<List<LearningCatalogItem>>
    suspend fun getSubthemes(themeId: String): CoachActivityResult<List<LearningCatalogItem>>
    suspend fun getLearningObjectives(themeId: String): CoachActivityResult<List<LearningCatalogItem>>
    suspend fun getTrainingLearningConfig(activityId: String): CoachActivityResult<TrainingLearningConfig>
    suspend fun updateTrainingLearningConfig(activityId: String, config: TrainingLearningConfig): CoachActivityResult<Unit>
}
