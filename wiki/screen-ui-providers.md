The **ScreenUiProvider** pattern is a mechanism used to decouple the "Chrome" of a screen (TopAppBar, FloatingActionButton) from the screen's content and the main navigation host.

## The Problem

In a modular app, the `MainScreen` (which holds the `Scaffold`) doesn't know about specific screens inside feature modules. We wanted to avoid a monolithic `when(route)` statement in `MainScreen` to determine which Title or FAB to show.

## The Solution

We define an interface `ScreenUiProvider` in **`:core:design-system`**. Each Feature module implements this interface for its screens to expose their UI requirements.

### 1. The Interface

```kotlin
interface ScreenUiProvider {
    val route: String
    val topBar: (@Composable () -> Unit)? get() = null
    val fab: (@Composable () -> Unit)? get() = null
}

```

### 2. Implementation (Feature Module)

In the feature module, we create an implementation binding it to a specific route. We use our custom **`DynamicTopAppBar`** here:

```kotlin
class ExpensesScreenUiProviderImpl(
    override val route: String = Routes.EXPENSES
) : ScreenUiProvider {

    override val topBar: @Composable () -> Unit = {
        DynamicTopAppBar(
            title = stringResource(R.string.expenses_title),
            subtitle = stringResource(R.string.expenses_subtitle)
        )
    }
    
    // Note: FAB is often null here if handled by the screen itself (see below)
}

```

> **⚠️ No placeholder actions.** The `actions` block in `DynamicTopAppBar` must ONLY contain `IconButton`s with **functional** `onClick` handlers. If a feature (Search, Filter, Info) is not yet implemented, **omit the `actions` parameter entirely** rather than rendering an icon with an empty handler. Dead icons erode user trust. See `wiki/ux-ui-concepts.md` § 3.3.

### 3. Dependency Injection

We register it in Koin as a `ScreenUiProvider`:

```kotlin
single { ExpensesScreenUiProviderImpl() } bind ScreenUiProvider::class

```

### 4. Consumption (The "Magic")

In `MainScreen`, we inject **all** providers and find the matching one for the current route:

```kotlin
val providers: List<ScreenUiProvider> = getKoin().getAll()
val currentProvider = providers.find { it.route == currentRoute }

Scaffold(
    topBar = { currentProvider?.topBar?.invoke() },
    floatingActionButton = { currentProvider?.fab?.invoke() }
) { content() }

```

---

## 🏗️ Design System Components

We have created specialized components to ensure consistency across providers:

### `DynamicTopAppBar`

A wrapper around Material 3's `LargeTopAppBar` that automatically handles:

* **Scroll Behavior:** Collapses smoothly when the list scrolls (hooked into `MainScreen`'s scroll state).
* **Title/Subtitle:** Supports a subtitle that fades out as the bar collapses.
* **Back Navigation:** Automatically shows the back arrow if an `onBack` callback is provided.

### `ExpressiveFab`

A custom FAB with organic shapes:

* **Idle:** A 7-point "star" (blob shape).
* **Pressed:** Morphs into a "flower" shape for tactile feedback.
* **Shared Element:** Supports `sharedTransitionKey` to expand into a full screen (e.g., creating a new Expense).
* **Idle Breathing (opt-in):** `enableIdleAnimation = true` adds a subtle vertical floating animation (~4px, 3s cycle).
* **Scroll-Aware Auto-Hide:** Screens wrap the FAB in `AnimatedVisibility` using `rememberScrollAwareFabVisibility(listState)` to hide on scroll-down and show on scroll-up.

---

## ⚠️ Important Exception: Shared Element Transitions

While `ScreenUiProvider` is excellent for static configuration, it has limitations for animations.

**If a FAB needs to animate into a new screen (Container Transform):**

1. Return `null` for `fab` in the `ScreenUiProvider`.
2. Render the `ExpressiveFab` **inside the Screen composable** itself.
3. This allows the FAB to access the `SharedTransitionScope` of the screen and animate correctly.

*Example: The Expenses List screen renders its own "Add Expense" FAB to support the explosion animation when clicked.*

## 🏷️ Screens Without FABs: Inline Card Actions

Not every screen needs a floating action button. When a screen has **multiple contextual actions** tied to a specific data card (e.g., the Balances screen's "Add Money" and "Withdraw Cash"), the preferred pattern is to embed action buttons **directly inside the card** as `FilledTonalButton`s. This reduces visual clutter, eliminates dual-FAB overlays, and places actions near the data they affect.

*Example: The Balances screen embeds "Add Money" and "Withdraw Cash" buttons inside `GroupPocketBalanceCard` instead of rendering two stacked `ExpressiveFab`s.*

