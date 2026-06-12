package com.civiq.app.di

import com.civiq.app.data.repository.AdminRepositoryImpl
import com.civiq.app.data.repository.AuthRepositoryImpl
import com.civiq.app.data.repository.DailyChallengeRepositoryImpl
import com.civiq.app.data.repository.FeatureFlagRepositoryImpl
import com.civiq.app.data.repository.GamificationRepositoryImpl
import com.civiq.app.data.repository.LeaderboardRepositoryImpl
import com.civiq.app.data.repository.NotificationRepositoryImpl
import com.civiq.app.data.repository.PreferencesRepositoryImpl
import com.civiq.app.data.repository.QuizRepositoryImpl
import com.civiq.app.data.repository.SubscriptionRepositoryImpl
import com.civiq.app.data.repository.UserRepositoryImpl
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.domain.repository.DailyChallengeRepository
import com.civiq.app.domain.repository.FeatureFlagRepository
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.domain.repository.LeaderboardRepository
import com.civiq.app.domain.repository.NotificationRepository
import com.civiq.app.domain.repository.PreferencesRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.domain.repository.SubscriptionRepository
import com.civiq.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each [domain repository][com.civiq.app.domain.repository] interface
 * to its Firebase-backed implementation. Note: [com.civiq.app.domain.repository.AiQuizRepository]
 * is bound separately in `AiModule`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository

    @Binds
    @Singleton
    abstract fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository

    @Binds
    @Singleton
    abstract fun bindDailyChallengeRepository(impl: DailyChallengeRepositoryImpl): DailyChallengeRepository

    @Binds
    @Singleton
    abstract fun bindLeaderboardRepository(impl: LeaderboardRepositoryImpl): LeaderboardRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindFeatureFlagRepository(impl: FeatureFlagRepositoryImpl): FeatureFlagRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
