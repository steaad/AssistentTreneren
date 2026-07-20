package com.example.assistenttreneren.feature.recording.domain.model

enum class RecordingSubCategory(
    val activityCategory: String,
    val displayName: String,
) {
    MatchFirstHalf(activityCategory = "Kamp", displayName = "1.omgang"),
    MatchBreak(activityCategory = "Kamp", displayName = "Pause"),
    MatchSecondHalf(activityCategory = "Kamp", displayName = "2.omgang"),
    MatchEvaluation(activityCategory = "Kamp", displayName = "Evaluering"),
    TrainingGame(activityCategory = "Trening", displayName = "Spill"),
    TrainingDrill(activityCategory = "Trening", displayName = "Øvelse"),
    TrainingEvaluation(activityCategory = "Trening", displayName = "Evaluering"),
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
