package es.pedrazamiguez.expenseshareapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.main.navigation.DeepLinkHolder
import es.pedrazamiguez.expenseshareapp.features.navigation.AppNavHost
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private var navHostController: NavHostController? = null
    private val deepLinkHolder: DeepLinkHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Save pending deep link before NavHost consumes the intent.
        // On cold start, if the user is not authenticated, the NavHost graph starts
        // with Routes.LOGIN — the deep link targets Routes.MAIN which doesn't exist
        // yet, so the intent is silently dropped. We preserve it here for replay
        // after the auth/onboarding flow completes.
        if (intent?.action == Intent.ACTION_VIEW && intent?.data != null) {
            deepLinkHolder.pendingDeepLink = intent.data
        }

        enableEdgeToEdge()
        setContent {
            ExpenseShareAppTheme {
                val navController = rememberNavController()
                navHostController = navController
                AppNavHost(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navHostController?.handleDeepLink(intent)
    }
}
