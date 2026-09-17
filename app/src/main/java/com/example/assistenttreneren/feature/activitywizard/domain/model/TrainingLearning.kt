package com.example.assistenttreneren.feature.activitywizard.domain.model

enum class TeamFunction {
    ATTACK,
    DEFENCE,
    ATTACK_TRANSITION,
    DEFENCE_TRANSITION,
    OFFENSIVE_SET_PIECES,
    DEFENSIVE_SET_PIECES,
}

data class LearningCatalogItem(
    val id: String,
    val name: String,
    val description: String?,
    val parentId: String?,
    val teamFunction: TeamFunction?,
)

data class TrainingLearningConfig(
    val teamFunction: TeamFunction? = null,
    val theme: LearningCatalogItem? = null,
    val subtheme: LearningCatalogItem? = null,
    val objectives: List<LearningCatalogItem> = emptyList(),
) {
    val isComplete: Boolean
        get() = teamFunction != null && theme != null && objectives.isNotEmpty()
}
