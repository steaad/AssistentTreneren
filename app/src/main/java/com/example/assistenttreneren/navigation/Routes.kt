package com.example.assistenttreneren.navigation

import android.net.Uri

sealed class Routes(
    val route: String,
) {
    data object Loading : Routes("loading")
    data object Login : Routes("login")
    data object Home : Routes("home")
    data object InitialPassword : Routes("initial_password")
    data object Settings : Routes("settings")
    data object CoachActivity : Routes("coach_activity")
    data object CoachActivityType : Routes("coach_activity/activity_type")
    data object CoachActivityAudioRecording : Routes("coach_activity/audio_recording")
    data object CoachActivityUpload : Routes("coach_activity/upload")
    data object CoachActivitySummary : Routes("coach_activity/summary")
    data object Analysis : Routes("analysis")
    data object AnalysisResult : Routes("analysis/result/{analysisId}") {
        fun createRoute(analysisId: String): String = "analysis/result/${Uri.encode(analysisId)}"
    }
    data object History : Routes("history")
}
