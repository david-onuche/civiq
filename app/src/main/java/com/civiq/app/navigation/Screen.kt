package com.civiq.app.navigation

/**
 * Single source of truth for all navigation routes in CiviQ.
 *
 * Routes with arguments expose a `createRoute(...)` factory so call sites
 * never hand-build path strings. Argument names are also exposed as
 * constants for use in `navArgument(...)` declarations in [CiviQNavGraph].
 */
sealed class Screen(val route: String) {

    // ---------------------------------------------------------------
    // Auth & onboarding
    // ---------------------------------------------------------------
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    // ---------------------------------------------------------------
    // Bottom navigation destinations
    // ---------------------------------------------------------------
    data object Home : Screen("home")
    data object QuizHub : Screen("quiz_hub")
    data object Challenges : Screen("challenges")
    data object Leaderboard : Screen("leaderboard")
    data object Profile : Screen("profile")

    // ---------------------------------------------------------------
    // Quiz flow
    // ---------------------------------------------------------------
    data object QuizDifficultySelect : Screen("quiz_difficulty/{$ARG_CATEGORY}") {
        fun createRoute(category: String) = "quiz_difficulty/$category"
    }

    data object QuizPlay : Screen(
        "quiz_play/{$ARG_CATEGORY}/{$ARG_DIFFICULTY}?$ARG_CHALLENGE_ID={$ARG_CHALLENGE_ID}"
    ) {
        fun createRoute(category: String, difficulty: String, challengeId: String? = null): String {
            val base = "quiz_play/$category/$difficulty"
            return if (challengeId != null) "$base?$ARG_CHALLENGE_ID=$challengeId" else base
        }
    }

    data object QuizResult : Screen("quiz_result/{$ARG_ATTEMPT_ID}") {
        fun createRoute(attemptId: String) = "quiz_result/$attemptId"
    }

    // ---------------------------------------------------------------
    // Profile & account
    // ---------------------------------------------------------------
    data object EditProfile : Screen("edit_profile")
    data object QuizHistory : Screen("quiz_history")
    data object Achievements : Screen("achievements")
    data object Settings : Screen("settings")
    data object Notifications : Screen("notifications")

    // ---------------------------------------------------------------
    // Premium
    // ---------------------------------------------------------------
    data object Premium : Screen("premium")
    data object AiCoach : Screen("ai_coach")

    // ---------------------------------------------------------------
    // Admin
    // ---------------------------------------------------------------
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminUsers : Screen("admin_users")
    data object AdminUserDetail : Screen("admin_users/{$ARG_USER_ID}") {
        fun createRoute(userId: String) = "admin_users/$userId"
    }
    data object AdminQuestions : Screen("admin_questions")
    data object AdminQuestionEditor : Screen("admin_questions/editor?$ARG_QUESTION_ID={$ARG_QUESTION_ID}") {
        fun createRoute(questionId: String? = null): String =
            if (questionId != null) "admin_questions/editor?$ARG_QUESTION_ID=$questionId"
            else "admin_questions/editor"
    }
    data object AdminChallenges : Screen("admin_challenges")
    data object AdminAchievements : Screen("admin_achievements")
    data object AdminFeatureFlags : Screen("admin_feature_flags")

    companion object {
        const val ARG_CATEGORY = "category"
        const val ARG_DIFFICULTY = "difficulty"
        const val ARG_CHALLENGE_ID = "challengeId"
        const val ARG_ATTEMPT_ID = "attemptId"
        const val ARG_USER_ID = "userId"
        const val ARG_QUESTION_ID = "questionId"
    }
}

/** Top-level navigation graph route names, used to scope nested [androidx.navigation.NavGraph]s. */
object NavGraphs {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
    const val ADMIN = "admin_graph"
}
