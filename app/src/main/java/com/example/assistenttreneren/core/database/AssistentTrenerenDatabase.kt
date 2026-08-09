package com.example.assistenttreneren.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.assistenttreneren.core.database.dao.AnalysisMetadataDao
import com.example.assistenttreneren.core.database.dao.CoachActivityDao
import com.example.assistenttreneren.core.database.dao.LocalRecordingDao
import com.example.assistenttreneren.core.database.dao.UploadJobDao
import com.example.assistenttreneren.core.database.entity.AnalysisMetadataEntity
import com.example.assistenttreneren.core.database.entity.CoachActivityEntity
import com.example.assistenttreneren.core.database.entity.LocalRecordingEntity
import com.example.assistenttreneren.core.database.entity.UploadJobEntity

@Database(
    entities = [
        CoachActivityEntity::class,
        LocalRecordingEntity::class,
        UploadJobEntity::class,
        AnalysisMetadataEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AssistentTrenerenDatabase : RoomDatabase() {
    abstract fun coachActivityDao(): CoachActivityDao

    abstract fun localRecordingDao(): LocalRecordingDao

    abstract fun uploadJobDao(): UploadJobDao

    abstract fun analysisMetadataDao(): AnalysisMetadataDao
}
