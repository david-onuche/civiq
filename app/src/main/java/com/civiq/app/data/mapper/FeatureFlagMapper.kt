package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.FeatureFlagDto
import com.civiq.app.domain.model.FeatureFlag

fun FeatureFlagDto.toDomain(): FeatureFlag = FeatureFlag(
    key = key,
    isEnabled = isEnabled,
    description = description,
    requiresPremium = requiresPremium,
)

fun FeatureFlag.toDto(): FeatureFlagDto = FeatureFlagDto(
    key = key,
    isEnabled = isEnabled,
    description = description,
    requiresPremium = requiresPremium,
)
