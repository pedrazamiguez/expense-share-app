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

---

## Non-Tab Feature Modules (`TabGraphContributor`)

Some features are **standalone write-flows** extracted into their own modules but navigated to from within an existing tab (not the root `AppNavHost`). These modules implement `TabGraphContributor` instead of `NavigationProvider`.

### How it works

1. The non-tab module defines a `TabGraphContributor` implementation that registers its composables.
2. The module registers it in Koin: `factory { ContributionsTabGraphContributorImpl() } bind TabGraphContributor::class`.
3. The host tab's `NavigationProvider` injects all `TabGraphContributor` instances and calls `contributeGraph(builder)` inside its `buildGraph()`.

### Current non-tab route ownership

| Route | Served by | Host Tab |
|---|---|---|
| `Routes.ADD_CONTRIBUTION` | `:features:contributions` | Balances tab |
| `Routes.ADD_CASH_WITHDRAWAL` | `:features:withdrawals` | Balances tab |
| `Routes.MANAGE_SUBUNITS` | `:features:subunits` | Groups tab |
| `Routes.CREATE_EDIT_SUBUNIT` | `:features:subunits` | Groups tab |

The host tab navigates to these routes via `LocalTabNavController.current.navigate(Routes.ADD_CONTRIBUTION)` — there is **no compile-time dependency** between the host tab module and the non-tab module. The routes are defined in `:core:design-system/Routes.kt` (shared by all modules).

