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

⚠️ **IMPORTANT:** You MUST wrap your component in `PreviewThemeWrapper` (or `ExpenseShareAppTheme`) for the theme colors to actually apply. The annotation only sets the environment; the wrapper applies the theme.

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

## ⚠️ Important: Theme Wrapper Required

**Why do I need `PreviewThemeWrapper`?**

The preview annotations like `@PreviewThemes` only set the **system UI mode** (light/dark). They don't automatically apply your app's Material 3 theme.

Your composables need to be inside `ExpenseShareAppTheme` (which `PreviewThemeWrapper` provides) to:
- Apply Material 3 color schemes
- Use theme typography
- Access theme shapes and elevation

**Without wrapper:**
```kotlin
@PreviewThemes  // Sets UI mode but doesn't apply theme
@Composable
private fun MyPreview() {
    Button(onClick = {}) { Text("Click") }  // ❌ Uses default Material colors, not your theme
}
```

**With wrapper:**
```kotlin
@PreviewThemes  // Sets UI mode
@Composable
private fun MyPreview() {
    PreviewThemeWrapper {  // ✅ Applies ExpenseShareAppTheme which reads isSystemInDarkTheme()
        Button(onClick = {}) { Text("Click") }  // Now uses your theme colors!
    }
}
```

## Helper Composables

### PreviewThemeWrapper
Wraps content in the app theme with a specific dark mode setting.

```kotlin
@Preview
@Composable
private fun CustomPreview() {
    PreviewThemeWrapper(darkTheme = true) {
        MyComponent()
    }
}
```

### PreviewLocaleWrapper
Wraps content with a specific locale configuration.

```kotlin
@Preview
@Composable
private fun SpanishPreview() {
    PreviewLocaleWrapper(locale = Locale("es")) {
        PreviewThemeWrapper {
            MyComponent()
        }
    }
}
```

### PreviewGrid
Shows all 4 combinations in a single preview with labeled sections.

```kotlin
@Preview(name = "All Combinations", showBackground = true)
@Composable
private fun AllCombinationsPreview() {
    PreviewGrid {
        MyComponent()
    }
}
```

## Best Practices

1. **Use `@PreviewComplete` for final components** - Ensures your component looks good in all scenarios

2. **Use `@PreviewThemes` during development** - Faster iteration when you're not concerned about locales

3. **Use `@PreviewLocales` for text-heavy components** - Focus on string resource validation

4. **Use `PreviewGrid` for documentation** - Great for showing all states in a single screenshot

5. **Wrap with `PreviewThemeWrapper`** - Always wrap your components in the theme unless testing a specific Material3 primitive

## Examples

### Simple Button
```kotlin
@PreviewComplete
@Composable
private fun SubmitButtonPreview() {
    Button(onClick = {}) {
        Text(stringResource(R.string.submit))
    }
}
```

### Complex Screen
```kotlin
@PreviewThemes
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        uiState = LoginUiState(),
        onEvent = {}
    )
}
```

### FAB with Shape Morphing
```kotlin
@PreviewComplete
@Composable
private fun ExpressiveFabPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        ExpressiveFab(
            onClick = {},
            icon = Icons.Outlined.Add,
            contentDescription = "Add"
        )
    }
}
```

## How It Works: The Two-Part System

Previewing themes requires **two parts** working together:

1. **Preview Annotation** (`@PreviewThemes`, `@PreviewComplete`)
    - Sets the system UI mode (light/dark)
    - Configures `isSystemInDarkTheme()` to return true/false
    - Does NOT apply your theme colors directly

2. **Theme Wrapper** (`PreviewThemeWrapper` or `ExpenseShareAppTheme`)
    - Wraps content in your app's Material 3 theme
    - Reads `isSystemInDarkTheme()` (set by annotation)
    - Applies the correct color scheme based on that value

**Think of it like this:**
- Annotation = "Tell the preview it's nighttime"
- Wrapper = "Turn on the night-mode lights"

Both are needed for your component to actually display with dark theme colors.

## Notes

- Previews are only for development and don't appear in the final APK
- Locale changes in previews don't affect the actual app configuration
- Use `showBackground = true` to see your component against the theme background
- Multi-preview annotations generate separate preview panes in Android Studio
- **Always use `PreviewThemeWrapper` when using theme-related annotations**

