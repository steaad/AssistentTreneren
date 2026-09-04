package com.example.assistenttreneren.feature.activitywizard.domain.model

data class CoachActivity(
    val activityId: String,
    val activityCategory: String?,
    val title: String?,
    val recordings: List<Recording>,
)

data class MatchRosterSuggestion(
    val sourceActivityId: String,
    val title: String,
    val playerNames: List<String>,
)
