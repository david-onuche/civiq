package com.civiq.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Local-only user preferences (theme, onboarding status, notification toggles)
 * that don't need to sync across devices via Firestore.
 */
interface PreferencesRepository {

    val isOnboardingComplete: Flow<Boolean>
    suspend fun setOnboardingComplete(complete: Boolean)

    val isDarkModeEnabled: Flow<Boolean?>
    suspend fun setDarkModeEnabled(enabled: Boolean)

    val isDynamicColorEnabled: Flow<Boolean>
    suspend fun setDynamicColorEnabled(enabled: Boolean)

    val areNotificationsEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
