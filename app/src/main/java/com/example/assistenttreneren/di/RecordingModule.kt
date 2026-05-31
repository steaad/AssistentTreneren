package com.example.assistenttreneren.di

import com.example.assistenttreneren.feature.recording.data.repository.RecordingRepositoryImpl
import com.example.assistenttreneren.feature.recording.domain.repository.RecordingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecordingModule {
    @Binds
    @Singleton
    abstract fun bindRecordingRepository(
        repository: RecordingRepositoryImpl,
    ): RecordingRepository
}
