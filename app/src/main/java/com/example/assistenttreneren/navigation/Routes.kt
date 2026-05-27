package com.example.assistenttreneren.navigation

sealed class Routes(
    val route: String,
) {
    data object Loading : Routes("loading")
    data object Login : Routes("login")
    data object Home : Routes("home")
    data object CoachActivity : Routes("coach_activity")
    data object Analysis : Routes("analysis")
    data object History : Routes("history")
}
