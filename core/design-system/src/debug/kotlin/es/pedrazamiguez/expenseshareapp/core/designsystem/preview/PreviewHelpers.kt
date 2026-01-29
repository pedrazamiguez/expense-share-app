package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme

/**
 * Wraps content in the app theme, automatically detecting the dark mode from the preview annotation.
 *
 * When used with `@PreviewThemes` or `@PreviewComplete`, this wrapper will automatically
 * read the UI mode set by the annotation and apply the correct theme.
 *
 * You can also override the dark theme setting manually for custom previews.
 *
 * Example with annotation (recommended):
 * ```
 * @PreviewThemes
 * @Composable
 * private fun MyPreview() {
 *     PreviewThemeWrapper {  // Automatically detects light/dark from annotation
 *         MyComponent()
 *     }
 * }
 * ```
 *
 * Example with manual override:
 * ```
 * @Preview
 * @Composable
 * private fun MyDarkPreview() {
 *     PreviewThemeWrapper(darkTheme = true) {  // Force dark theme
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Composable
fun PreviewThemeWrapper(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ExpenseShareAppTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
