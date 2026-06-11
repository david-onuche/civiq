package com.civiq.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * CiviQ favors generously rounded corners to convey a friendly,
 * playful, "learning game" feel inspired by Duolingo / Brilliant.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Additional shapes used for specific gamified components (cards, pills, avatars). */
object CiviQExtraShapes {
    val pill = RoundedCornerShape(50)
    val badge = RoundedCornerShape(12.dp)
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
