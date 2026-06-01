package com.example.assistenttreneren.feature.recording.domain.model

enum class RecordingSubCategory(
    val activityCategory: String,
    val displayName: String,
) {
    MatchFirstHalf(activityCategory = "Kamp", displayName = "1.omgang"),
    MatchBreak(activityCategory = "Kamp", displayName = "Pause"),
    MatchSecondHalf(activityCategory = "Kamp", displayName = "2.omgang"),
    TrainingGame(activityCategory = "Trening", displayName = "Spill"),
    TrainingDrill(activityCategory = "Trening", displayName = "Øvelse"),
    MeetingPlayer(activityCategory = "Møte", displayName = "Spillermøte"),
    MeetingCoach(activityCategory = "Møte", displayName = "Trenermøte"),
    ScoutingPlayer(activityCategory = "Speiding", displayName = "Enkeltspiller"),
    ScoutingOpponent(activityCategory = "Speiding", displayName = "Motstander");

    companion object {
        fun forActivityCategory(activityCategory: String?): List<RecordingSubCategory> =
            entries.filter { subCategory ->
                subCategory.activityCategory == activityCategory
            }
    }
}
