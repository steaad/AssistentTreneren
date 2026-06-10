package com.example.assistenttreneren.feature.activitywizard.data.repository

import com.example.assistenttreneren.core.database.dao.CoachActivityDao
import com.example.assistenttreneren.feature.activitywizard.data.mapper.toCoachActivity
import com.example.assistenttreneren.feature.activitywizard.data.mapper.toCoachActivityEntity
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import com.example.assistenttreneren.feature.activitywizard.domain.repository.LocalCoachActivityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalCoachActivityRepositoryImpl @Inject constructor(
    private val coachActivityDao: CoachActivityDao,
) : LocalCoachActivityRepository {
    override fun observeActivities(): Flow<List<CoachActivity>> =
        coachActivityDao.observeActivities()
            .map { activities -> activities.map { it.toCoachActivity() } }

    override fun observeActivity(activityId: String): Flow<CoachActivity?> =
        coachActivityDao.observeActivity(activityId)
            .map { it?.toCoachActivity() }

    override suspend fun saveActivity(activity: CoachActivity) {
        coachActivityDao.upsertActivity(activity.toCoachActivityEntity())
    }

    override suspend fun saveActivities(activities: List<CoachActivity>) {
        coachActivityDao.upsertActivities(
            activities.map { it.toCoachActivityEntity() },
        )
    }
}
