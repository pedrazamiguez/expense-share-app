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
        uiState.errorMessage != null -> { ErrorView(...) }
        uiState.items.isEmpty() -> { EmptyStateView(...) }
        else -> { LazyColumn { ... } }
    }
}
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

## 6. Wizard Pattern Evaluation: Add Expense Form

**Context:** As part of the form refactoring epic (#714), the Add Expense form was evaluated for conversion to a multi-step wizard (like `AddCashWithdrawalScreen`).

**Decision:** Keep the current **single-page progressive disclosure** pattern. Do NOT adopt the wizard.

**Rationale:**

1. **Quick-add is the primary use case.** The majority of expenses require only amount + title + submit. A wizard would force every user through multi-step navigation even for simple entries, adding friction where none is needed.

2. **Progressive disclosure already works well.** The "More details" toggle reveals payment method, category, split, vendor, notes, payment status, due date, and receipt — all on a single scrollable page. Users can fill exactly the fields they need without step navigation.

3. **No sequential data dependencies.** Unlike cash withdrawals (where the exchange rate step depends on the currency chosen in the amount step, and the fee step depends on the details toggle), expense fields are independent and can be filled in any order. A wizard imposes an artificial ordering that doesn't match the domain.

4. **State management overhead.** Adding wizard navigation would require `AddExpenseStep` enum, `currentStep`/`applicableSteps`/`isCurrentStepValid` computed properties in `AddExpenseUiState`, `NextStep`/`PreviousStep` events, step-clamping logic, and per-step validation — significant complexity for marginal UX benefit.

5. **Existing modularisation is sufficient.** The form is already decomposed into focused section composables (`QuickAddSection`, `ExchangeRateSection`, `PaymentMethodSection`, `CategorySection`, `SplitSection`, `VendorNotesSection`, `PaymentStatusSection`, `DueDateSection`, `ReceiptSection`, `AddOnsSection`), each under 60 lines. The `ExpandableDetailsSection` is a thin orchestrator.

**When to reconsider:** If user research reveals that the progressive disclosure toggle is frequently missed, or if the form grows new sections with sequential dependencies (e.g., an approval workflow), re-evaluate the hybrid approach: keep quick-add as a single page and offer a wizard for "detailed expense" mode behind a toggle.

**Reference:** See `AddCashWithdrawalScreen` and `AddContributionScreen` for the wizard pattern applied to forms with sequential dependencies.
