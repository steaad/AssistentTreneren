package com.example.assistenttreneren.di

import com.example.assistenttreneren.feature.activitywizard.data.repository.CoachActivityRepositoryImpl
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import com.example.assistenttreneren.feature.activitywizard.data.repository.TrainingLearningRepositoryImpl
import com.example.assistenttreneren.feature.activitywizard.domain.repository.TrainingLearningRepository
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

    @Binds
    abstract fun bindTrainingLearningRepository(
        trainingLearningRepositoryImpl: TrainingLearningRepositoryImpl,
    ): TrainingLearningRepository
}
