package com.civiq.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.civiq.app.R

/** Defines the five primary destinations shown in CiviQ's bottom navigation bar. */
enum class BottomNavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(
        screen = Screen.Home,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    QUIZ(
        screen = Screen.QuizHub,
        labelRes = R.string.nav_quiz,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
    ),
    CHALLENGES(
        screen = Screen.Challenges,
        labelRes = R.string.nav_challenges,
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today,
    ),
    LEADERBOARD(
        screen = Screen.Leaderboard,
        labelRes = R.string.nav_leaderboard,
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
    ),
    PROFILE(
        screen = Screen.Profile,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
}
