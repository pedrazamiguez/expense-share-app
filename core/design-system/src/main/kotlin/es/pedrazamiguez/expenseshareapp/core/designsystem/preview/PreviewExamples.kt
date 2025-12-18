package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.R

/**
 * Example usage of the preview annotations and helpers.
 *
 * This file demonstrates how to preview components in multiple locales and themes.
 */

// ============================================================================
// Example 1: Using PreviewLocales annotation (2 previews: EN, ES)
// PreviewThemeWrapper ensures consistent theme styling across locales
// ============================================================================

@PreviewLocales
@Composable
private fun ButtonPreview_Locales() {
    PreviewThemeWrapper {  // Provides consistent theme for both locales
        Button(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.example_action_accept))
        }
    }
}

// ============================================================================
// Example 2: Using PreviewThemes annotation (2 previews: Light, Dark)
// ⚠️ IMPORTANT: PreviewThemeWrapper is REQUIRED for themes to work!
// ============================================================================

@PreviewThemes
@Composable
private fun ButtonPreview_Themes() {
    PreviewThemeWrapper {  // This wrapper is required! It reads isSystemInDarkTheme()
        Button(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.example_action_send))
        }
    }
}

// ============================================================================
// Example 3: Using PreviewComplete annotation (4 previews: all combinations)
// ⚠️ IMPORTANT: PreviewThemeWrapper is REQUIRED for themes to work!
// ============================================================================

@PreviewComplete
@Composable
private fun ButtonPreview_Complete() {
    PreviewThemeWrapper {  // This wrapper is required!
        Button(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.example_action_sign_in))
        }
    }
}

// ============================================================================
// Example 4: Manual control with PreviewThemeWrapper
// ============================================================================

@Preview(name = "Custom Dark Theme", showBackground = true)
@Composable
private fun ButtonPreview_CustomDark() {
    PreviewThemeWrapper(darkTheme = true) {
        Button(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.example_action_download))
        }
    }
}

