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
import androidx.compose.runtime.LaunchedEffect
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
import com.civiq.app.presentation.achievements.AchievementsScreen
import com.civiq.app.presentation.admin.AdminAchievementsScreen
import com.civiq.app.presentation.admin.AdminChallengesScreen
import com.civiq.app.presentation.admin.AdminDashboardScreen
import com.civiq.app.presentation.admin.AdminFeatureFlagsScreen
import com.civiq.app.presentation.admin.AdminQuestionEditorScreen
import com.civiq.app.presentation.admin.AdminQuestionsScreen
import com.civiq.app.presentation.admin.AdminUserDetailScreen
import com.civiq.app.presentation.admin.AdminUsersScreen
import com.civiq.app.presentation.aicoach.AiCoachScreen
import com.civiq.app.presentation.challenges.ChallengesScreen
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.home.HomeScreen
import com.civiq.app.presentation.leaderboard.LeaderboardScreen
import com.civiq.app.presentation.notifications.NotificationsScreen
import com.civiq.app.presentation.premium.PremiumScreen
import com.civiq.app.presentation.profile.ProfileScreen
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
 * Sub-screens not yet implemented render [ComingSoonDetailScreen] as a
 * temporary placeholder; each is replaced with its real screen as later
 * phases land.
 */
@Composable
fun MainScreen(onSignedOut: () -> Unit, pendingDeepLinkRoute: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(pendingDeepLinkRoute) {
        pendingDeepLinkRoute?.let { route -> navController.navigate(route) }
    }

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
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
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
            composable(Screen.Achievements.route) {
                AchievementsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Challenges.route) {
                ChallengesScreen(
                    onStartChallenge = { category, difficulty, challengeId ->
                        navController.navigate(Screen.QuizPlay.createRoute(category.name, difficulty.name, challengeId))
                    },
                )
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onSignedOut = onSignedOut,
                    onNavigateToQuizHistory = { navController.navigate(Screen.QuizHistory.route) },
                    onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) },
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                    onNavigateToAiCoach = { navController.navigate(Screen.AiCoach.route) },
                    onNavigateToAdminDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                )
            }
            composable(Screen.EditProfile.route) {
                ComingSoonDetailScreen(
                    title = stringResource(R.string.profile_menu_edit_profile),
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable(Screen.Settings.route) {
                ComingSoonDetailScreen(
                    title = stringResource(R.string.profile_menu_settings),
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { route -> navController.navigate(route) },
                )
            }
            composable(Screen.Premium.route) {
                PremiumScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.AiCoach.route) {
                AiCoachScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                )
            }
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                    onNavigateToQuestions = { navController.navigate(Screen.AdminQuestions.route) },
                    onNavigateToChallenges = { navController.navigate(Screen.AdminChallenges.route) },
                    onNavigateToAchievements = { navController.navigate(Screen.AdminAchievements.route) },
                    onNavigateToFeatureFlags = { navController.navigate(Screen.AdminFeatureFlags.route) },
                )
            }
            composable(Screen.AdminUsers.route) {
                AdminUsersScreen(
                    onBackClick = { navController.popBackStack() },
                    onUserClick = { userId -> navController.navigate(Screen.AdminUserDetail.createRoute(userId)) },
                )
            }
            composable(
                route = Screen.AdminUserDetail.route,
                arguments = listOf(navArgument(Screen.ARG_USER_ID) { type = NavType.StringType }),
            ) {
                AdminUserDetailScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.AdminQuestions.route) {
                AdminQuestionsScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddQuestionClick = { navController.navigate(Screen.AdminQuestionEditor.createRoute()) },
                    onQuestionClick = { questionId ->
                        navController.navigate(Screen.AdminQuestionEditor.createRoute(questionId))
                    },
                )
            }
            composable(
                route = Screen.AdminQuestionEditor.route,
                arguments = listOf(
                    navArgument(Screen.ARG_QUESTION_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                AdminQuestionEditorScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.AdminChallenges.route) {
                AdminChallengesScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.AdminAchievements.route) {
                AdminAchievementsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.AdminFeatureFlags.route) {
                AdminFeatureFlagsScreen(onBackClick = { navController.popBackStack() })
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

/** Placeholder for sub-screens not yet implemented, with a top bar so users can navigate back. */
@Composable
private fun ComingSoonDetailScreen(title: String, onBackClick: () -> Unit) {
    Scaffold(
        topBar = { CiviQTopAppBar(title = title, onBackClick = onBackClick) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.common_coming_soon), style = MaterialTheme.typography.titleMedium)
        }
    }
}
