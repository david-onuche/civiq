package com.civiq.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.AppNotification
import com.civiq.app.domain.model.User
import com.civiq.app.domain.usecase.notification.NotificationUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the Notifications screen: the user's in-app notification feed and read-state actions. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModel @Inject constructor(
    private val notificationUseCases: NotificationUseCases,
) : ViewModel() {

    private val currentUser: StateFlow<User?> = notificationUseCases.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<Resource<List<AppNotification>>> = currentUser
        .filterNotNull()
        .flatMapLatest { user -> notificationUseCases.observeNotifications(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())

    /** Marks [notification] as read when tapped, e.g. before following its deep link. */
    fun onNotificationClick(notification: AppNotification) {
        if (notification.isRead) return
        viewModelScope.launch {
            notificationUseCases.markNotificationAsRead(notification.id)
        }
    }

    fun markAllAsRead() {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            notificationUseCases.markAllNotificationsAsRead(userId)
        }
    }
}
