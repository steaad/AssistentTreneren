package com.example.assistenttreneren.feature.activitywizard.data.mapper

import com.example.assistenttreneren.feature.activitywizard.data.dto.CoachActivityDto
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import com.example.assistenttreneren.feature.activitywizard.domain.model.Recording

fun CoachActivityDto.toCoachActivity(): CoachActivity =
    CoachActivity(
        activityId = activityId,
        activityCategory = activityCategory,
        title = title,
        recordings = recordings.map { recording ->
            Recording(
                id = recording.id,
                recordingType = recording.recordingType,
                filename = recording.filename,
                duration = recording.duration,
            )
        },
    )
