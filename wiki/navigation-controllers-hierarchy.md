The app uses a nested navigation strategy to handle Bottom Tabs and full-screen flows properly.

## 1. LocalRootNavController

* **Scope:** Global (The entire Activity).
* **Provider:** `AppNavHost`.
* **Usage:**
* Auth flows (Login -> Onboarding).
* Full-screen features that should cover the BottomBar (e.g., Settings, creating a new Group, adding an Expense).
* Back navigation from "leaf" screens.



## 2. LocalTabNavController

* **Scope:** Local (Inside `MainScreen` tabs).
* **Provider:** `MainScreen`.
* **Usage:**
* Navigation *within* a specific tab (e.g., Groups List -> Group Detail).
* Preserves the back stack of each tab independently.



## How to use

We use `CompositionLocal` to access them easily in our composables without passing them down as parameters everywhere.

```kotlin
// 1. To navigate to a full-screen feature (hiding bottom bar)
// Used by FABs or Settings items
val rootNav = LocalRootNavController.current
rootNav.navigate(Routes.SETTINGS)

// 2. To navigate inside the current tab (keeping bottom bar visible)
// Used by list items to drill down to details
val tabNav = LocalTabNavController.current
tabNav.navigate(Routes.GROUP_DETAIL) 

```

> **Pro Tip:** In `MainScreen`, we explicitly capture the `LocalTabNavController` for the active tab and expose it via `CompositionLocalProvider`. This ensures that any child component (like a list item inside the Expenses tab) automatically gets the *correct* controller for that tab when it calls `LocalTabNavController.current`.
