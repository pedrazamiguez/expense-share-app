# Preview Utilities

This package provides annotations and helper composables for previewing components in multiple locales and themes.

## Quick Reference

| Annotation | Previews Generated | Use Case |
|------------|-------------------|----------|
| `@PreviewLocales` | 2 (EN, ES) | Test string resources in both languages |
| `@PreviewThemes` | 2 (Light, Dark) | Test component styling in both themes |
| `@PreviewComplete` | 4 (EN Light, EN Dark, ES Light, ES Dark) | Comprehensive testing of all combinations |

## Annotations

### @PreviewLocales
Shows your component in English and Spanish.

```kotlin
@PreviewLocales
@Composable
private fun MyComponentPreview() {
    PreviewThemeWrapper {
        MyComponent()
    }
}
```

### @PreviewThemes
Shows your component in Light and Dark modes.

⚠️ **IMPORTANT:** You MUST wrap your component in `PreviewThemeWrapper` for the theme colors to actually apply. The annotation sets the environment; the wrapper reads it and applies the theme.

```kotlin
@PreviewThemes
@Composable
private fun MyComponentPreview() {
    PreviewThemeWrapper {  // Required for themes to work!
        MyComponent()
    }
}
```

### @PreviewComplete
Shows your component in all 4 combinations (EN Light, EN Dark, ES Light, ES Dark).

⚠️ **IMPORTANT:** Wrap in `PreviewThemeWrapper` for themes to work!

```kotlin
@PreviewComplete
@Composable
private fun MyComponentPreview() {
    PreviewThemeWrapper {  // Required for themes to work!
        MyComponent()
    }
}
```

## ⚠️ Important: How It Works

**Why do I need `PreviewThemeWrapper`?**

The preview annotations like `@PreviewThemes` only set the **system UI mode** (light/dark). They don't automatically apply your app's Material 3 theme.

`PreviewThemeWrapper` uses `isSystemInDarkTheme()` internally, which reads the UI mode set by the preview annotation. This is what makes the theme respond correctly to light/dark previews.

**Without wrapper:**
```kotlin
@PreviewThemes  // Sets UI mode but nothing reads it
@Composable
private fun MyPreview() {
    Button(onClick = {}) { Text("Click") }  // ❌ Uses default Material colors
}
```

**With wrapper:**
```kotlin
@PreviewThemes  // Sets UI mode to light/dark
@Composable
private fun MyPreview() {
    PreviewThemeWrapper {  // ✅ Reads isSystemInDarkTheme() and applies correct theme
        Button(onClick = {}) { Text("Click") }  // Now uses your theme colors!
    }
}
```

## Helper Composable

### PreviewThemeWrapper
Wraps content in the app theme, automatically detecting the dark mode from the preview annotation.

```kotlin
// Automatic detection from annotation (recommended)
@PreviewThemes
@Composable
private fun MyPreview() {
    PreviewThemeWrapper {  // Automatically reads light/dark from annotation
        MyComponent()
    }
}

// Manual override
@Preview
@Composable
private fun ForcedDarkPreview() {
    PreviewThemeWrapper(darkTheme = true) {  // Force dark theme
        MyComponent()
    }
}
```

## Best Practices

1. **Always use `PreviewThemeWrapper`** when using `@PreviewThemes` or `@PreviewComplete` annotations

2. **Use `@PreviewComplete` for final components** - Ensures your component looks good in all scenarios

3. **Use `@PreviewThemes` during development** - Faster iteration when you're not concerned about locales

4. **Use `@PreviewLocales` for text-heavy components** - Focus on string resource validation

## Examples

### Simple Button
```kotlin
@PreviewComplete
@Composable
private fun SubmitButtonPreview() {
    PreviewThemeWrapper {
        Button(onClick = {}) {
            Text(stringResource(R.string.submit))
        }
    }
}
```

### Screen Preview
```kotlin
@PreviewThemes
@Composable
private fun LoginScreenPreview() {
    PreviewThemeWrapper {
        LoginScreen(
            uiState = LoginUiState(),
            onEvent = {}
        )
    }
}
```

## How It Works: The Two-Part System

Previewing themes requires **two parts** working together:

1. **Preview Annotation** (`@PreviewThemes`, `@PreviewComplete`)
    - Sets the system UI mode (light/dark) via `uiMode` parameter
    - This makes `isSystemInDarkTheme()` return true/false accordingly
    - Does NOT apply your theme colors directly

2. **Theme Wrapper** (`PreviewThemeWrapper`)
    - Wraps content in `ExpenseShareAppTheme`
    - Reads `isSystemInDarkTheme()` (which responds to the annotation's uiMode)
    - Applies the correct color scheme based on that value

**Think of it like this:**
- Annotation = "Tell the system it's nighttime"
- Wrapper = "Apply the night-mode theme colors"

Both are needed for your component to actually display with dark theme colors.

## Notes

- Previews are only for development and don't appear in the final APK
- Locale changes in previews don't affect the actual app configuration
- Use `showBackground = true` to see your component against the theme background
- Multi-preview annotations generate separate preview panes in Android Studio
- **Always use `PreviewThemeWrapper` when using theme-related annotations**
