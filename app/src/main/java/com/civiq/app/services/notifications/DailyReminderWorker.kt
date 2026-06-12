package com.civiq.app.services.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.civiq.app.R
import com.civiq.app.data.local.datastore.UserPreferencesDataSource
import com.civiq.app.domain.model.NotificationType
import com.civiq.app.domain.usecase.challenge.ChallengeUseCases
import com.civiq.app.navigation.Screen
import com.civiq.app.utils.Resource
import com.civiq.app.utils.toDateId
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Runs once a day around the user's preferred reminder hour. If the signed-in
 * user hasn't completed today's daily challenge yet, shows a local reminder
 * notification nudging them to do so.
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val challengeUseCases: ChallengeUseCases,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val notificationBuilder: CiviQNotificationBuilder,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!userPreferencesDataSource.areNotificationsEnabled.first()) return Result.success()

        val user = challengeUseCases.observeCurrentUser().first() ?: return Result.success()
        if (user.isGuest) return Result.success()

        val challenge = (challengeUseCases.getTodayChallenge() as? Resource.Success)?.data
            ?: return Result.success()

        val today = System.currentTimeMillis().toDateId()
        val progress = challengeUseCases.observeDailyChallengeProgress(user.id, today).first()
        val isCompleted = (progress as? Resource.Success)?.data?.isCompleted == true
        if (isCompleted) return Result.success()

        notificationBuilder.show(
            type = NotificationType.DAILY_QUIZ,
            title = applicationContext.getString(R.string.notification_daily_reminder_title),
            body = applicationContext.getString(R.string.notification_daily_reminder_body, challenge.title),
            deepLinkRoute = Screen.Challenges.route,
        )
        return Result.success()
    }
}
