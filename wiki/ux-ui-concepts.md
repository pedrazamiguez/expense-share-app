This project implements specific User Experience (UX) principles to ensure the app feels polished, responsive, and native.

## 1. Progressive Disclosure & Delayed Navigation

**Context:** Selection screens (e.g., *Default Currency*).

When a user selects an item in a list that acts as a definitive action (single selection), we shouldn't force them to press "Back". However, navigating back immediately feels abrupt and leaves the user wondering if the tap "registered".

**Implementation:**

1. User taps item.
2. **Immediate Feedback:** The UI updates (checkbox appears) instantly.
3. **Delay:** We wait for `200ms` (defined as `UiConstants.NAV_FEEDBACK_DELAY`).
4. **Action:** Navigation pops back automatically.

## 2. Preventing FOUC & Skeleton Loading

**Context:** Loading async data (e.g., Fetching Expenses or User Preferences).

**The Problem:**
If we initialize a ViewModel state with a default value (e.g., "EUR") while loading the real value (e.g., "USD"), the user sees a "flicker": *EUR -> USD* in a split second. Similarly, showing a blank white screen while data loads makes the app feel slow.

**The Solution:**

* **ViewModel:** Initialize state as `null` or `Loading`.
* **Small UI (Text):** Use an invisible placeholder (e.g., `Modifier.alpha(0f)`) to reserve space and prevent layout shifts.
* **Complex UI (Lists):** Use **Shimmer (Skeleton) Loading**.

We use a standard `ShimmerLoadingList` component that mimics the layout of the actual content (Cards, Rows) with a pulsating gradient. This reduces perceived wait time.

**Anti-Flicker: Deferred Loading Container**

When responses are fast (< 150ms), showing a shimmer skeleton and immediately replacing it with content creates an ugly flicker — the "flash of loading state". We solve this with `DeferredLoadingContainer`:

1.  **Show delay** (`LOADING_SHOW_DELAY_MS = 150ms`): The shimmer is NOT shown immediately. If data arrives within this window, the shimmer is skipped entirely and content appears instantly.
2.  **Minimum display time** (`LOADING_MIN_DISPLAY_TIME_MS = 500ms`): If the shimmer *does* appear (because loading took longer than the delay), it stays visible for at least 500ms so it doesn't flash and disappear.
3.  **Visual continuity on reload**: When content was previously displayed and a reload starts (`isLoading` transitions from `false` to `true`) *while the composable stays in composition*, the previous content remains visible during the show-delay window instead of rendering a blank frame. This smooths over brief reloads (e.g., pull-to-refresh or a `stateIn` resubscribe on the same screen). On first-ever load, or when the composable has been removed from composition (e.g., switching away from a tab whose content is disposed and then returning), the behavior is unchanged — a blank frame is shown during the delay. Cross-tab visual continuity is instead handled at the flow layer by `FLOW_REPLAY_EXPIRATION`, which resets the `stateIn` replay cache to `initialValue`.

```kotlin
// UI Implementation — wrap with DeferredLoadingContainer
DeferredLoadingContainer(
    isLoading = uiState.isLoading,
    loadingContent = { ShimmerLoadingList() }
) {
    when {
        uiState.items.isEmpty() -> { EmptyStateView(...) }
        else -> { LazyColumn { ... } }
    }
}
// Transient errors (network failures, validation) are shown via LocalSnackbarController,
// not inline error views. See SnackbarController for the standard pattern.
```

Both timing constants live in `UiConstants` and can be overridden per call-site if a specific screen needs different thresholds.

## 3. Micro-Interactions & Delight

**Context:** The Primary Floating Action Button (FAB).

Material Design 3 encourages "Expressive" motion. We avoid static, boring interactions.

**Implementation:**
The `ExpressiveFab` uses a custom `Morph` shape:

* **Idle State:** An organic 7-point "blob" or "star" shape.
* **Pressed State:** Smoothly morphs into a rounded "flower" shape.
* **Touch Feedback:** Scales down slightly (`0.9x`) on press.

These subtle animations make the app feel tactile and alive without blocking the user's task.

## 4. Edge-to-Edge & Glassmorphism

**Context:** The Main Screen and Bottom Navigation.

We strictly follow modern Android **Edge-to-Edge** guidelines.

* **Content:** Lists scroll *behind* the Bottom Navigation Bar and Status Bar. We manually handle `WindowInsets` to ensure the last item is not obscured.
* **Glass Effect:** The Bottom Bar uses a library called **Haze** to create a real-time blur effect (frosted glass) over the scrolling content behind it, providing depth and context.

## 5. Explicit Empty States

**Context:** Lists with no data.

An empty white screen looks like a bug. We use a standardized `EmptyStateView` component when a list is valid but empty.

**Guidelines:**

* **Icon:** Large, outlined icon relevant to the feature.
* **Title:** Clear statement ("No expenses yet").
* **Description:** Helpful guidance ("Tap the + button to add one").
* **Alignment:** Centered vertically and horizontally.

## 6. Stepped Wizard Pattern

**Context:** All multi-field forms in the app are migrated to a stepped wizard pattern for consistency (epic #714).

**Pattern:**
Every form that collects more than a couple of fields uses a shared `WizardStepIndicator` + `AnimatedContent` step transitions + `WizardNavigationBar`. Each step is a focused composable (≤ 60 lines).

**Implementation highlights:**

1. **Step Enum with Factory:** Each form defines an enum (e.g., `AddExpenseStep`, `CashWithdrawalStep`) with a `companion object` containing an `applicableSteps(...)` factory that dynamically includes/excludes conditional steps based on form state.
2. **Conditional Steps & Clamping:** When a state change removes a conditional step (e.g., switching currency back to the group currency removes the EXCHANGE_RATE step), the `withStepClamped()` method on the UiState ensures `currentStep` falls back to the nearest valid step.
3. **Wizard Navigation in ViewModel:** `NextStep` and `PreviousStep` events are handled inline in the ViewModel. On the first step, `PreviousStep` emits a `NavigateBack` UiAction that the Feature routes to `navController.popBackStack()`.
4. **BackHandler:** The Feature composable intercepts the system back button via `BackHandler` and delegates to `PreviousStep`, so the wizard navigates backward before exiting.
5. **Step-Skipping Strategy:** Future work (issue #719) will add a skip strategy for optional steps. For now, the user must go through all applicable steps.

**Reference implementations:**
- `AddCashWithdrawalScreen` / `CashWithdrawalStep` — the original wizard reference.
- `AddExpenseScreen` / `AddExpenseStep` — 11 steps: TITLE → PAYMENT_METHOD → AMOUNT → EXCHANGE_RATE (conditional) → SPLIT (conditional) → CATEGORY → VENDOR_NOTES → PAYMENT_STATUS → RECEIPT → ADD_ONS → REVIEW.
