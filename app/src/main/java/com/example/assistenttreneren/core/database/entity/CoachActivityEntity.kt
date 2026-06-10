package com.example.assistenttreneren.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coach_activities")
data class CoachActivityEntity(
    @PrimaryKey
    val activityId: String,
    val activityCategory: String?,
    val title: String?,
    val updatedAtMillis: Long,
)
