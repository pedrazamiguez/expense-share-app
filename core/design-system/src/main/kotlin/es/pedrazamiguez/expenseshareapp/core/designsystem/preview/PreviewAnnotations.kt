package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview annotation that shows the component in both English and Spanish.
 *
 * **Note:** Wrap your component in `PreviewThemeWrapper` to apply theme colors.
 *
 * Example:
 * ```
 * @PreviewLocales
 * @Composable
 * private fun MyPreview() {
 *     PreviewThemeWrapper {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Spanish", locale = "es", showBackground = true)
annotation class PreviewLocales

/**
 * Preview annotation that shows the component in both Light and Dark modes.
 *
 * **Important:** This annotation only sets the UI mode environment.
 * You MUST wrap your component in `PreviewThemeWrapper` (or `ExpenseShareAppTheme`)
 * for the light/dark colors to actually apply.
 *
 * Example:
 * ```
 * @PreviewThemes
 * @Composable
 * private fun MyPreview() {
 *     PreviewThemeWrapper {  // Required!
 *         MyComponent()
 *     }
 * }
 * ```
 *
 * The `PreviewThemeWrapper` reads `isSystemInDarkTheme()` which responds to the uiMode.
 */
@Preview(name = "Light Mode", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class PreviewThemes

/**
 * Preview annotation that shows the component in all combinations of locales and themes.
 * This will generate 4 previews: EN Light, EN Dark, ES Light, ES Dark.
 *
 * **Note:** Wrap your component in `PreviewThemeWrapper` to apply theme colors.
 *
 * Example:
 * ```
 * @PreviewComplete
 * @Composable
 * private fun MyPreview() {
 *     PreviewThemeWrapper {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Preview(name = "EN - Light", locale = "en", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN - Dark", locale = "en", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "ES - Light", locale = "es", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "ES - Dark", locale = "es", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class PreviewComplete

