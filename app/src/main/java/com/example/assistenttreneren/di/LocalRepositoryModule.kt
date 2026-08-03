package com.example.assistenttreneren.di

import com.example.assistenttreneren.feature.activitywizard.data.repository.LocalCoachActivityRepositoryImpl
import com.example.assistenttreneren.feature.activitywizard.domain.repository.LocalCoachActivityRepository
import com.example.assistenttreneren.feature.analysis.data.repository.LocalAnalysisRepositoryImpl
import com.example.assistenttreneren.feature.analysis.domain.repository.LocalAnalysisRepository
import com.example.assistenttreneren.feature.recording.data.repository.LocalRecordingRepositoryImpl
import com.example.assistenttreneren.feature.recording.domain.repository.LocalRecordingRepository
import com.example.assistenttreneren.feature.upload.data.repository.LocalUploadRepositoryImpl
import com.example.assistenttreneren.feature.upload.domain.repository.LocalUploadRepository
import com.example.assistenttreneren.feature.transcription.data.repository.TranscriptionReviewRepositoryImpl
import com.example.assistenttreneren.feature.transcription.domain.repository.TranscriptionReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLocalCoachActivityRepository(
        repository: LocalCoachActivityRepositoryImpl,
    ): LocalCoachActivityRepository

    @Binds
    @Singleton
    abstract fun bindLocalRecordingRepository(
        repository: LocalRecordingRepositoryImpl,
    ): LocalRecordingRepository

    @Binds
    @Singleton
    abstract fun bindLocalUploadRepository(
        repository: LocalUploadRepositoryImpl,
    ): LocalUploadRepository

    @Binds
    @Singleton
    abstract fun bindLocalAnalysisRepository(
        repository: LocalAnalysisRepositoryImpl,
    ): LocalAnalysisRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptionReviewRepository(
        repository: TranscriptionReviewRepositoryImpl,
    ): TranscriptionReviewRepository
}
