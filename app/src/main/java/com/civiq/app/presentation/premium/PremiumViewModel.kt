package com.civiq.app.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.PaymentProvider
import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.SubscriptionTier
import com.civiq.app.domain.model.User
import com.civiq.app.domain.usecase.premium.PremiumUseCases
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the Premium paywall/management screen: subscription status, pricing tiers, and subscribe/cancel actions. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PremiumViewModel @Inject constructor(
    private val premiumUseCases: PremiumUseCases,
) : ViewModel() {

    private val currentUser: StateFlow<User?> = premiumUseCases.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val subscriptionResource: StateFlow<Resource<Subscription?>> = currentUser
        .filterNotNull()
        .flatMapLatest { user -> premiumUseCases.observeSubscription(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())

    private val isProcessing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<UiText?>(null)

    val uiState: StateFlow<PremiumUiState> = combine(
        currentUser,
        subscriptionResource,
        isProcessing,
        errorMessage,
    ) { user, subscription, processing, error ->
        PremiumUiState(
            isLoading = user == null || subscription is Resource.Loading,
            user = user,
            subscription = (subscription as? Resource.Success)?.data,
            isProcessing = processing,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PremiumUiState())

    /** Starts (or renews) a subscription for the signed-in user, e.g. after a mock checkout confirmation. */
    fun subscribe(tier: SubscriptionTier, provider: PaymentProvider = PaymentProvider.GOOGLE_PLAY) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            isProcessing.value = true
            val result = premiumUseCases.subscribe(userId, tier, provider)
            if (result is Resource.Error) errorMessage.value = result.message
            isProcessing.value = false
        }
    }

    fun cancelSubscription() {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            isProcessing.value = true
            val result = premiumUseCases.cancelSubscription(userId)
            if (result is Resource.Error) errorMessage.value = result.message
            isProcessing.value = false
        }
    }

    fun dismissError() {
        errorMessage.update { null }
    }
}
