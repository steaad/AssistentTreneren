package com.example.assistenttreneren.feature.activitywizard.data.mapper

import com.example.assistenttreneren.feature.activitywizard.data.dto.CoachActivityDto
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity

fun CoachActivityDto.toCoachActivity(): CoachActivity =
    CoachActivity(
        activityId = activityId,
        activityCategory = activityCategory,
        title = title,
    )
