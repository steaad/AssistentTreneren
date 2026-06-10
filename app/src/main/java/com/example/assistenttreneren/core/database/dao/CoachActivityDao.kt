package com.example.assistenttreneren.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.assistenttreneren.core.database.entity.CoachActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachActivityDao {
    @Upsert
    suspend fun upsertActivity(activity: CoachActivityEntity)

    @Upsert
    suspend fun upsertActivities(activities: List<CoachActivityEntity>)

    @Query("SELECT * FROM coach_activities ORDER BY updatedAtMillis DESC")
    fun observeActivities(): Flow<List<CoachActivityEntity>>

    @Query("SELECT * FROM coach_activities WHERE activityId = :activityId")
    fun observeActivity(activityId: String): Flow<CoachActivityEntity?>

    @Query("SELECT * FROM coach_activities WHERE activityId = :activityId")
    suspend fun getActivity(activityId: String): CoachActivityEntity?
}
