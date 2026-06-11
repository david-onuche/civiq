package com.civiq.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.civiq.app.R
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.home.HomeScreen
import com.civiq.app.presentation.quiz.QuizDifficultySelectScreen
import com.civiq.app.presentation.quiz.QuizHistoryScreen
import com.civiq.app.presentation.quiz.QuizHubScreen
import com.civiq.app.presentation.quiz.QuizPlayScreen
import com.civiq.app.presentation.quiz.QuizResultScreen
import com.civiq.app.utils.safeEnumValueOf

/**
 * Hosts the bottom-navigation tabs (Home, Quiz, Challenges, Leaderboard,
 * Profile) behind their own [NavHost], nested inside [CiviQNavGraph]'s
 * [NavGraphs.MAIN] destination.
 *
 * Destinations not yet implemented render [ComingSoonScreen] as a temporary
 * placeholder; each is replaced with its real screen as later phases land.
 */
@Composable
fun MainScreen(onSignedOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { CiviQBottomNavigationBar(navController) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartQuiz = { category, difficulty, challengeId ->
                        navController.navigate(Screen.QuizPlay.createRoute(category.name, difficulty.name, challengeId))
                    },
                    onNavigateToQuizHub = { navController.navigateToBottomNavRoute(Screen.QuizHub.route) },
                    onSignedOut = onSignedOut,
                )
            }
            composable(Screen.QuizHub.route) {
                QuizHubScreen(
                    onSelectCategory = { category ->
                        navController.navigate(Screen.QuizDifficultySelect.createRoute(category.name))
                    },
                    onNavigateToHistory = { navController.navigate(Screen.QuizHistory.route) },
                )
            }
            composable(
                route = Screen.QuizDifficultySelect.route,
                arguments = listOf(navArgument(Screen.ARG_CATEGORY) { type = NavType.StringType }),
            ) { backStackEntry ->
                val category = safeEnumValueOf(
                    backStackEntry.arguments?.getString(Screen.ARG_CATEGORY),
                    QuizCategory.DEMOCRACY,
                )
                QuizDifficultySelectScreen(
                    category = category,
                    onSelectDifficulty = { difficulty ->
                        navController.navigate(Screen.QuizPlay.createRoute(category.name, difficulty.name))
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.QuizPlay.route,
                arguments = listOf(
                    navArgument(Screen.ARG_CATEGORY) { type = NavType.StringType },
                    navArgument(Screen.ARG_DIFFICULTY) { type = NavType.StringType },
                    navArgument(Screen.ARG_CHALLENGE_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                QuizPlayScreen(
                    onNavigateToResult = { attemptId ->
                        navController.navigate(Screen.QuizResult.createRoute(attemptId)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onExit = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.QuizResult.route,
                arguments = listOf(navArgument(Screen.ARG_ATTEMPT_ID) { type = NavType.StringType }),
            ) {
                QuizResultScreen(
                    onDone = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                    onRetry = { category, difficulty ->
                        navController.navigate(Screen.QuizPlay.createRoute(category.name, difficulty.name)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                )
            }
            composable(Screen.QuizHistory.route) {
                QuizHistoryScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Challenges.route) {
                ComingSoonScreen(title = stringResource(R.string.nav_challenges))
            }
            composable(Screen.Leaderboard.route) {
                ComingSoonScreen(title = stringResource(R.string.nav_leaderboard))
            }
            composable(Screen.Profile.route) {
                ComingSoonScreen(title = stringResource(R.string.nav_profile))
            }
        }
    }
}

@Composable
private fun CiviQBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToBottomNavRoute(item.screen.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
            )
        }
    }
}

/** Switches to a bottom-nav tab, preserving each tab's back stack and avoiding duplicate destinations. */
private fun NavHostController.navigateToBottomNavRoute(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Placeholder for bottom-nav destinations not yet implemented. */
@Composable
private fun ComingSoonScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}
