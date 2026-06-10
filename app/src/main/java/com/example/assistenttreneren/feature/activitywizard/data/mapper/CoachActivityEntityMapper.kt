package com.example.assistenttreneren.feature.activitywizard.data.mapper

import com.example.assistenttreneren.core.database.entity.CoachActivityEntity
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity

fun CoachActivityEntity.toCoachActivity(): CoachActivity =
    CoachActivity(
        activityId = activityId,
        activityCategory = activityCategory,
        title = title,
        recordings = emptyList(),
    )

fun CoachActivity.toCoachActivityEntity(
    updatedAtMillis: Long = System.currentTimeMillis(),
): CoachActivityEntity =
    CoachActivityEntity(
        activityId = activityId,
        activityCategory = activityCategory,
        title = title,
        updatedAtMillis = updatedAtMillis,
    )
