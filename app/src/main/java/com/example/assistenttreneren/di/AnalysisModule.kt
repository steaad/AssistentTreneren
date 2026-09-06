package com.example.assistenttreneren.di

import com.example.assistenttreneren.feature.analysis.data.repository.AnalysisWorkflowRepositoryImpl
import com.example.assistenttreneren.feature.analysis.domain.repository.AnalysisWorkflowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisModule {
    @Binds
    abstract fun bindAnalysisWorkflowRepository(
        repository: AnalysisWorkflowRepositoryImpl,
    ): AnalysisWorkflowRepository
}
