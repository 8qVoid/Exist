package com.exist.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.exist.app.presentation.navigation.ExistNavHost
import com.exist.app.ui.theme.ExistTheme

class MainActivity : ComponentActivity() {

    private var startRoute: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startRoute = intent?.getStringExtra(EXTRA_START_ROUTE)

        setContent {
            ExistTheme(darkTheme = true) {
                ExistNavHost(
                    startRouteOverride = startRoute,
                    onConsumeStartRoute = { startRoute = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startRoute = intent.getStringExtra(EXTRA_START_ROUTE)
    }

    companion object {
        const val EXTRA_START_ROUTE = "start_route"
        const val ROUTE_CAMERA = "camera"
    }
}
