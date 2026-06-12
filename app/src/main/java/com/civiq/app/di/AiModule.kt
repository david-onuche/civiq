package com.civiq.app.di

import com.civiq.app.data.repository.AiCoachRepositoryImpl
import com.civiq.app.data.repository.AiQuizRepositoryImpl
import com.civiq.app.domain.repository.AiCoachRepository
import com.civiq.app.domain.repository.AiQuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds [AiQuizRepository] and [AiCoachRepository] to their Gemini/OpenAI-backed implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiQuizRepository(impl: AiQuizRepositoryImpl): AiQuizRepository

    @Binds
    @Singleton
    abstract fun bindAiCoachRepository(impl: AiCoachRepositoryImpl): AiCoachRepository
}
