package com.example.assistenttreneren.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.assistenttreneren.core.database.AssistentTrenerenDatabase
import com.example.assistenttreneren.core.database.dao.AnalysisMetadataDao
import com.example.assistenttreneren.core.database.dao.CoachActivityDao
import com.example.assistenttreneren.core.database.dao.LocalRecordingDao
import com.example.assistenttreneren.core.database.dao.UploadJobDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AssistentTrenerenDatabase =
        Room.databaseBuilder(
            context,
            AssistentTrenerenDatabase::class.java,
            DATABASE_NAME,
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideCoachActivityDao(
        database: AssistentTrenerenDatabase,
    ): CoachActivityDao = database.coachActivityDao()

    @Provides
    fun provideLocalRecordingDao(
        database: AssistentTrenerenDatabase,
    ): LocalRecordingDao = database.localRecordingDao()

    @Provides
    fun provideUploadJobDao(
        database: AssistentTrenerenDatabase,
    ): UploadJobDao = database.uploadJobDao()

    @Provides
    fun provideAnalysisMetadataDao(
        database: AssistentTrenerenDatabase,
    ): AnalysisMetadataDao = database.analysisMetadataDao()

    private const val DATABASE_NAME = "assistent_treneren.db"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE local_recordings ADD COLUMN mediaType TEXT NOT NULL DEFAULT 'Audio'",
            )
            db.execSQL(
                "ALTER TABLE local_recordings ADD COLUMN mimeType TEXT NOT NULL DEFAULT 'audio/mp4'",
            )
        }
    }
}
