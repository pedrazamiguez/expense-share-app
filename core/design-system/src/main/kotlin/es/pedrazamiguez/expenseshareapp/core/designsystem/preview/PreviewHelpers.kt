package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import java.util.Locale

/**
 * Wraps content in the app theme with the specified dark mode setting.
 * Useful for programmatic preview generation.
 */
@Composable
fun PreviewThemeWrapper(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    ExpenseShareAppTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

/**
 * Wraps content with a specific locale configuration.
 * Useful for testing string resources in different languages.
 */
@Composable
fun PreviewLocaleWrapper(
    locale: Locale,
    content: @Composable () -> Unit
) {
    val configuration = Configuration(LocalConfiguration.current).apply {
        setLocale(locale)
    }

    val context = LocalContext.current.createConfigurationContext(configuration)

    CompositionLocalProvider(
        LocalContext provides context,
        LocalConfiguration provides configuration
    ) {
        content()
    }
}

/**
 * Shows the content in a grid with labels for all theme and locale combinations.
 * Perfect for comprehensive component testing.
 */
@Composable
fun PreviewGrid(
    content: @Composable () -> Unit
) {
    Column {
        // English Light
        PreviewSection(title = "English - Light") {
            PreviewLocaleWrapper(locale = Locale.ENGLISH) {
                PreviewThemeWrapper(darkTheme = false) {
                    content()
                }
            }
        }

        // English Dark
        PreviewSection(title = "English - Dark") {
            PreviewLocaleWrapper(locale = Locale.ENGLISH) {
                PreviewThemeWrapper(darkTheme = true) {
                    content()
                }
            }
        }

        // Spanish Light
        PreviewSection(title = "Spanish - Light") {
            PreviewLocaleWrapper(locale = Locale.forLanguageTag("es")) {
                PreviewThemeWrapper(darkTheme = false) {
                    content()
                }
            }
        }

        // Spanish Dark
        PreviewSection(title = "Spanish - Dark") {
            PreviewLocaleWrapper(locale = Locale.forLanguageTag("es")) {
                PreviewThemeWrapper(darkTheme = true) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(4.dp)
        )
        Box(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

