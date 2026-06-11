package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `feature_flags/{key}`. See docs/DATABASE.md. */
data class FeatureFlagDto(
    @DocumentId val key: String = "",
    val isEnabled: Boolean = false,
    val description: String = "",
    val requiresPremium: Boolean = false,
)
