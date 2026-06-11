package com.civiq.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CiviqBlue40,
    onPrimary = Color.White,
    primaryContainer = CiviqBlue90,
    onPrimaryContainer = CiviqBlue10,
    secondary = CiviqGold40,
    onSecondary = Color.White,
    secondaryContainer = CiviqGold90,
    onSecondaryContainer = CiviqGold10,
    tertiary = CiviqGreen40,
    onTertiary = Color.White,
    tertiaryContainer = CiviqGreen90,
    onTertiaryContainer = CiviqGreen10,
    error = CiviqRed40,
    onError = Color.White,
    errorContainer = CiviqRed90,
    onErrorContainer = CiviqRed10,
    background = CiviqNeutral99,
    onBackground = CiviqNeutral10,
    surface = CiviqNeutral99,
    onSurface = CiviqNeutral10,
    surfaceVariant = CiviqNeutralVariant90,
    onSurfaceVariant = CiviqNeutralVariant30,
    outline = CiviqNeutralVariant50,
)

private val DarkColorScheme = darkColorScheme(
    primary = CiviqBlue80,
    onPrimary = CiviqBlue20,
    primaryContainer = CiviqBlue30,
    onPrimaryContainer = CiviqBlue90,
    secondary = CiviqGold80,
    onSecondary = CiviqGold20,
    secondaryContainer = CiviqGold30,
    onSecondaryContainer = CiviqGold90,
    tertiary = CiviqGreen80,
    onTertiary = CiviqGreen20,
    tertiaryContainer = CiviqGreen30,
    onTertiaryContainer = CiviqGreen90,
    error = CiviqRed80,
    onError = CiviqRed20,
    errorContainer = CiviqRed30,
    onErrorContainer = CiviqRed90,
    background = CiviqNeutral10,
    onBackground = CiviqNeutral90,
    surface = CiviqNeutral10,
    onSurface = CiviqNeutral90,
    surfaceVariant = CiviqNeutralVariant30,
    onSurfaceVariant = CiviqNeutralVariant80,
    outline = CiviqNeutralVariant50,
)

/**
 * Semantic colors used by gamification UI (XP, coins, streaks, difficulty
 * badges) that don't map cleanly onto Material3's [androidx.compose.material3.ColorScheme] roles.
 */
data class ExtendedColorScheme(
    val xp: Color,
    val xpContainer: Color,
    val coin: Color,
    val coinContainer: Color,
    val streak: Color,
    val streakContainer: Color,
    val correct: Color,
    val correctContainer: Color,
    val incorrect: Color,
    val incorrectContainer: Color,
    val difficultyBeginner: Color,
    val difficultyIntermediate: Color,
    val difficultyAdvanced: Color,
    val difficultyExpert: Color,
)

private val LightExtendedColors = ExtendedColorScheme(
    xp = XpPurple,
    xpContainer = XpPurpleContainer,
    coin = CoinGold,
    coinContainer = CoinGoldContainer,
    streak = StreakOrange,
    streakContainer = StreakOrangeContainer,
    correct = CorrectGreen,
    correctContainer = CorrectGreenContainer,
    incorrect = IncorrectRed,
    incorrectContainer = IncorrectRedContainer,
    difficultyBeginner = DifficultyBeginner,
    difficultyIntermediate = DifficultyIntermediate,
    difficultyAdvanced = DifficultyAdvanced,
    difficultyExpert = DifficultyExpert,
)

private val DarkExtendedColors = LightExtendedColors.copy(
    xpContainer = XpPurple.copy(alpha = 0.24f),
    coinContainer = CoinGold.copy(alpha = 0.24f),
    streakContainer = StreakOrange.copy(alpha = 0.24f),
    correctContainer = CorrectGreen.copy(alpha = 0.24f),
    incorrectContainer = IncorrectRed.copy(alpha = 0.24f),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * CiviQ application theme.
 *
 * @param darkTheme whether to use the dark color scheme. Defaults to the system setting.
 * @param dynamicColor whether to use Material You dynamic color on Android 12+. Defaults to false
 *   so the CiviQ brand identity remains consistent; can be exposed as a user setting.
 */
@Composable
fun CiviQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Convenience accessor: `MaterialTheme.extendedColors.xp` etc. */
val MaterialTheme.extendedColors: ExtendedColorScheme
    @Composable
    get() = LocalExtendedColors.current
