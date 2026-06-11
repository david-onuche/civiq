package com.civiq.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.civiq.app.presentation.auth.ForgotPasswordScreen
import com.civiq.app.presentation.auth.LoginScreen
import com.civiq.app.presentation.auth.OnboardingScreen
import com.civiq.app.presentation.auth.RegisterScreen
import com.civiq.app.presentation.auth.SplashScreen

/**
 * CiviQ's root navigation graph: Splash decides between onboarding, the auth
 * flow (Login/Register/ForgotPassword), or the main app ([NavGraphs.MAIN],
 * see [MainScreen]). Successful auth and sign-out clear the back stack so the
 * system back button never returns to a stale auth/main screen.
 */
@Composable
fun CiviQNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(NavGraphs.MAIN) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onAuthenticated = {
                    navController.navigate(NavGraphs.MAIN) { popUpTo(0) }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onAuthenticated = {
                    navController.navigate(NavGraphs.MAIN) { popUpTo(0) }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavGraphs.MAIN) {
            MainScreen(
                onSignedOut = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
            )
        }
    }
}
