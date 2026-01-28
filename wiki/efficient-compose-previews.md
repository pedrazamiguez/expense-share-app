To avoid writing repetitive `@Preview` annotations for every component, this project includes a set of **Multi-Preview Annotations** in the `:core:design-system` module. These allow you to instantly visualize your components across different Languages (English/Spanish) and Themes (Light/Dark).

## 🔑 The Golden Rule: `PreviewThemeWrapper`

Standard `@Preview` annotations only configure the IDE's environment (e.g., "Set UI mode to Night"). They do **not** automatically apply your app's theme colors.

**You must wrap your preview content in `PreviewThemeWrapper`**.

```kotlin
@PreviewThemes
@Composable
fun MyComponentPreview() {
    PreviewThemeWrapper { // <--- REQUIRED!
        // It detects the environment (Light/Dark) and applies the correct colors
        MyComponent()
    }
}

```

---

## 🛠️ Available Annotations

### 1. `@PreviewLocales`

**Use case:** Text-heavy components where you need to verify translations and layout constraints (e.g., if Spanish text overflows).

* **Generates:** 2 Previews (English, Spanish).
* **Source:** `es` vs `en` locales.

```kotlin
@PreviewLocales
@Composable
private fun TextPreview() {
    PreviewThemeWrapper {
        Text(text = stringResource(id = R.string.welcome_message))
    }
}

```

### 2. `@PreviewThemes`

**Use case:** Icons, Buttons, or Cards where checking color contrast is critical.

* **Generates:** 2 Previews (Light Mode, Dark Mode).
* **Note:** Relies on `PreviewThemeWrapper` to read `isSystemInDarkTheme()`.

```kotlin
@PreviewThemes
@Composable
private fun IconPreview() {
    PreviewThemeWrapper {
        Icon(imageVector = Icons.Default.Home, contentDescription = null)
    }
}

```

### 3. `@PreviewComplete`

**Use case:** Full Screens or complex "Feature" components. This is the "Golden Master" check.

* **Generates:** 4 Previews (EN-Light, EN-Dark, ES-Light, ES-Dark).

```kotlin
@PreviewComplete
@Composable
private fun FullScreenPreview() {
    PreviewThemeWrapper {
        LoginScreen(...)
    }
}

```

---

## ⚡ Best Practices

1. **Don't Overuse `@PreviewComplete`:** It generates 4x renderings per composable. For simple atoms (like a spacer or divider), a standard preview or `@PreviewThemes` is faster for the IDE to render.
2. **Mock Data:** Always provide dummy data to your components so the preview renders immediately without needing a running ViewModel.
3. **Backgrounds:** All custom annotations have `showBackground = true` enabled by default, so you can clearly see the component's bounds.
