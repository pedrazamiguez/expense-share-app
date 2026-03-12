package es.pedrazamiguez.expenseshareapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.navigation.AppNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseShareAppTheme {
                AppNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

}
