package es.pedrazamiguez.expenseshareapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import es.pedrazamiguez.expenseshareapp.core.ui.designsystem.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        enableEdgeToEdge()
        setContent {
            ExpenseShareAppTheme {
                AppNavHost()
            }
        }
    }
}
