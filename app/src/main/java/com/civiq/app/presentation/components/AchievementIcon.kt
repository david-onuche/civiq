package com.civiq.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps an [com.civiq.app.domain.model.Achievement.iconName] (an admin-configurable
 * string stored in Firestore) to a Material icon, falling back to a trophy
 * for unrecognized names so newly added badges always render something.
 */
fun achievementIcon(iconName: String): ImageVector = when (iconName) {
    "EmojiEvents" -> Icons.Filled.EmojiEvents
    "Star" -> Icons.Filled.Star
    "LocalFireDepartment" -> Icons.Filled.LocalFireDepartment
    "School" -> Icons.Filled.School
    "WorkspacePremium" -> Icons.Filled.WorkspacePremium
    "Bolt" -> Icons.Filled.Bolt
    "Public" -> Icons.Filled.Public
    "MenuBook" -> Icons.Filled.MenuBook
    "Diversity3" -> Icons.Filled.Diversity3
    "Verified" -> Icons.Filled.Verified
    "Gavel" -> Icons.Filled.Gavel
    "HowToVote" -> Icons.Filled.HowToVote
    "AutoAwesome" -> Icons.Filled.AutoAwesome
    else -> Icons.Filled.EmojiEvents
}
