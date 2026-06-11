package com.civiq.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.repository.PreferencesRepository
import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Where [SplashScreen] should navigate once startup state has resolved. */
enum class SplashDestination {
    LOADING,
    ONBOARDING,
    AUTH,
    MAIN,
}

/**
 * Determines the app's initial destination by combining onboarding completion
 * (local DataStore) with the current authentication state (Firebase Auth +
 * Firestore user document, or guest session).
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val destination: StateFlow<SplashDestination> = combine(
        observeCurrentUser(),
        preferencesRepository.isOnboardingComplete,
    ) { user, onboardingComplete ->
        when {
            !onboardingComplete -> SplashDestination.ONBOARDING
            user != null -> SplashDestination.MAIN
            else -> SplashDestination.AUTH
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SplashDestination.LOADING,
    )
}
