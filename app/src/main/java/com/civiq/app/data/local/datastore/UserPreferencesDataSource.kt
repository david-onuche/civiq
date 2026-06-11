package com.civiq.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.civiq.app.utils.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the app's [DataStore] of [Preferences] for small, frequently-read
 * local settings (theme, onboarding status, notification prefs) that don't
 * belong in Firestore.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey(DataStoreKeys.KEY_DARK_MODE)
        val DYNAMIC_COLOR = booleanPreferencesKey(DataStoreKeys.KEY_DYNAMIC_COLOR)
        val ONBOARDING_COMPLETE = booleanPreferencesKey(DataStoreKeys.KEY_ONBOARDING_COMPLETE)
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey(DataStoreKeys.KEY_NOTIFICATIONS_ENABLED)
        val DAILY_REMINDER_HOUR = intPreferencesKey(DataStoreKeys.KEY_DAILY_REMINDER_HOUR)
        val PREFERRED_COUNTRY_CODE = stringPreferencesKey(DataStoreKeys.KEY_PREFERRED_COUNTRY_CODE)
        val LAST_SYNCED_FCM_TOKEN = stringPreferencesKey(DataStoreKeys.KEY_LAST_SYNCED_FCM_TOKEN)
        val CACHED_DAILY_CHALLENGE_ID = stringPreferencesKey(DataStoreKeys.KEY_CACHED_DAILY_CHALLENGE_ID)
    }

    val isDarkModeEnabled: Flow<Boolean?> = dataStore.data.map { it[Keys.DARK_MODE] }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    val isDynamicColorEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    val areNotificationsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    val dailyReminderHour: Flow<Int> = dataStore.data.map { it[Keys.DAILY_REMINDER_HOUR] ?: 18 }

    suspend fun setDailyReminderHour(hour: Int) {
        dataStore.edit { it[Keys.DAILY_REMINDER_HOUR] = hour }
    }

    val preferredCountryCode: Flow<String?> = dataStore.data.map { it[Keys.PREFERRED_COUNTRY_CODE] }

    suspend fun setPreferredCountryCode(countryCode: String) {
        dataStore.edit { it[Keys.PREFERRED_COUNTRY_CODE] = countryCode }
    }

    val lastSyncedFcmToken: Flow<String?> = dataStore.data.map { it[Keys.LAST_SYNCED_FCM_TOKEN] }

    suspend fun setLastSyncedFcmToken(token: String) {
        dataStore.edit { it[Keys.LAST_SYNCED_FCM_TOKEN] = token }
    }

    val cachedDailyChallengeId: Flow<String?> = dataStore.data.map { it[Keys.CACHED_DAILY_CHALLENGE_ID] }

    suspend fun setCachedDailyChallengeId(id: String) {
        dataStore.edit { it[Keys.CACHED_DAILY_CHALLENGE_ID] = id }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
