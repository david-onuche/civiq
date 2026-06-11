package com.civiq.app.data.repository

import com.civiq.app.data.local.datastore.UserPreferencesDataSource
import com.civiq.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Thin [PreferencesRepository] wrapper around [UserPreferencesDataSource]. */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : PreferencesRepository {

    override val isOnboardingComplete: Flow<Boolean> = dataSource.isOnboardingComplete

    override suspend fun setOnboardingComplete(complete: Boolean) =
        dataSource.setOnboardingComplete(complete)

    override val isDarkModeEnabled: Flow<Boolean?> = dataSource.isDarkModeEnabled

    override suspend fun setDarkModeEnabled(enabled: Boolean) =
        dataSource.setDarkModeEnabled(enabled)

    override val isDynamicColorEnabled: Flow<Boolean> = dataSource.isDynamicColorEnabled

    override suspend fun setDynamicColorEnabled(enabled: Boolean) =
        dataSource.setDynamicColorEnabled(enabled)

    override val areNotificationsEnabled: Flow<Boolean> = dataSource.areNotificationsEnabled

    override suspend fun setNotificationsEnabled(enabled: Boolean) =
        dataSource.setNotificationsEnabled(enabled)
}
