package com.example.assistenttreneren.feature.activitywizard.domain.repository

import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import kotlinx.coroutines.flow.Flow

interface LocalCoachActivityRepository {
    fun observeActivities(): Flow<List<CoachActivity>>

    fun observeActivity(activityId: String): Flow<CoachActivity?>

    suspend fun saveActivity(activity: CoachActivity)

    suspend fun saveActivities(activities: List<CoachActivity>)
}
