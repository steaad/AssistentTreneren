package com.example.assistenttreneren.di

import com.example.assistenttreneren.feature.activitywizard.data.repository.CoachActivityRepositoryImpl
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoachActivityModule {
    @Binds
    abstract fun bindCoachActivityRepository(
        coachActivityRepositoryImpl: CoachActivityRepositoryImpl,
    ): CoachActivityRepository
}
