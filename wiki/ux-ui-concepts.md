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

```kotlin
// UI Implementation
if (state.isLoading) {
    ShimmerLoadingList()
} else {
    LazyColumn { ... }
}

```

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
