package com.civiq.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Persists onboarding completion so [SplashViewModel] never routes back here. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingComplete(true)
        }
    }
}
