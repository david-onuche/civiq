package com.civiq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.civiq.app.navigation.CiviQNavGraph
import com.civiq.app.presentation.theme.CiviQTheme
import com.civiq.app.services.notifications.EXTRA_DEEP_LINK_ROUTE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        val pendingDeepLinkRoute = intent.getStringExtra(EXTRA_DEEP_LINK_ROUTE)

        setContent {
            CiviQTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CiviQNavGraph(pendingDeepLinkRoute = pendingDeepLinkRoute)
                }
            }
        }
    }
}
