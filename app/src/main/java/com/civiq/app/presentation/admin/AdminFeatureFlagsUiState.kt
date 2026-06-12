package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText

data class AdminFeatureFlagsUiState(
    val flags: Resource<List<FeatureFlag>> = Resource.Loading(),
    val updatingKeys: Set<String> = emptySet(),
    val errorMessage: UiText? = null,
)
