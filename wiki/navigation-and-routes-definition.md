The navigation system is string-based and centralized in the **`:core:design-system`** module.

## Defining a Route

Add a `const val` in `Routes.kt` (located in `:core:design-system`). This acts as the unique key for the destination.

```kotlin
object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val SETTINGS_DEFAULT_CURRENCY = "settings_default_currency"
}

```

## Registering the Route

1. **Feature Module:** Create a navigation extension function (e.g., `SettingsNavigation.kt`).
2. **Graph Construction:** Add the composable to the graph. For features with multiple screens, we typically wrap them in a `navigation` block (Nested Graph).

```kotlin
fun NavGraphBuilder.settingsGraph() {
    // Nested navigation graph for the Settings feature
    navigation(startDestination = Routes.SETTINGS_MAIN, route = Routes.SETTINGS) {
        
        composable(Routes.SETTINGS_MAIN) {
            SettingsFeature()
        }

        composable(Routes.SETTINGS_DEFAULT_CURRENCY) {
            DefaultCurrencyFeature()
        }
    }
}

```

## Adding to the App

In the `:features` module, open `AppNavHost.kt` and call the feature graph function inside the main `NavHost`:

```kotlin
NavHost(navController = navController, ...) {
    // ... other graphs
    settingsGraph()
}

```

> **Note:** For top-level bottom navigation tabs (like Groups or Expenses), we use the **`NavigationProvider`** interface instead of manually calling functions in `AppNavHost`. This allows the Main Screen to discover tabs dynamically via Koin.
