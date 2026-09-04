package com.example.assistenttreneren.feature.activitywizard.domain.repository

import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import com.example.assistenttreneren.feature.activitywizard.domain.model.MatchRosterSuggestion

interface CoachActivityRepository {
    suspend fun createActivity(): CoachActivityResult<CoachActivity>

    suspend fun updateActivity(
        activityId: String,
        activityCategory: String?,
        title: String?,
    ): CoachActivityResult<CoachActivity>

    suspend fun getActivities(): CoachActivityResult<List<CoachActivity>>

    suspend fun getActivity(
        activityId: String,
    ): CoachActivityResult<CoachActivity>

    suspend fun getMatchRoster(activityId: String): CoachActivityResult<List<String>>
    suspend fun getMatchRosterSuggestions(): CoachActivityResult<List<MatchRosterSuggestion>>
    suspend fun updateMatchRoster(activityId: String, playerNames: List<String>): CoachActivityResult<List<String>>
}
