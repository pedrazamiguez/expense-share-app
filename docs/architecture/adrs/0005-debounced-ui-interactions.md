# ADR-0005: Mandatory Debounced Modifiers for UI Navigation & Actions

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** presentation, ui, compose, debouncing, navigation, race-conditions

---

## Context and Problem Statement

In Jetpack Compose, rapid user taps on clickable list items, action buttons, or floating action buttons can dispatch multiple click events before a navigation transition or network side-effect completes. This results in duplicate destinations being pushed onto the back stack, multiple duplicate network requests, and visual glitches.

How do we systematically prevent duplicate click events and double-navigation bugs across all UI components?

## Decision Drivers

* Elimination of double-tap navigation race conditions.
* Protection against duplicate form submissions or API triggers.
* Consistent debounce timing across the entire design system.
* Easy adoption and compliance enforcement for developers and AI agents.

## Considered Options

1. **Manual In-ViewModel Debouncing:** Handling debounce flags or job cancellations inside every ViewModel event handler. Highly error-prone and easy to forget.
2. **Standard Compose `Modifier.clickable` with NavHost deduplication:** Deduplicating routes at the NavController layer. Leaves non-navigation actions unprotected.
3. **Mandatory Custom Debounced Modifiers in Design System (Chosen):** `Modifier.debouncedClickable` and `Modifier.debouncedCombinedClickable` as strict replacements for standard click modifiers on all actionable UI elements.

## Decision Outcome

Chosen option: **Option 3 (Mandatory Custom Debounced Modifiers in Design System)**.

### Architectural Rules
1. **Prohibition of Standard Clickables:** Standard `Modifier.clickable` and `Modifier.combinedClickable` must NEVER be used directly on list items, action buttons, cards, or FABs that trigger navigation or side-effects.
2. **Standard Debounce Modifiers:**
   - Use `Modifier.debouncedClickable(debounceInterval = ..., onClick = ...)` from `:core:design-system`.
   - Use `Modifier.debouncedCombinedClickable(...)` for items requiring long-press or double-click support.
3. **Design System Integration:** All custom design system buttons (`GradientButton`, `SecondaryButton`, `DestructiveButton`, `ExpressiveFab`) must encapsulate debounced click handling internally.

## Consequences

### Positive
* Complete prevention of multi-stack push anomalies during screen transitions.
* Single consistent debounce duration (`UiConstants.DEBOUNCE_DELAY_MS`) across the app.
* Cleaner ViewModel code without repetitive debounce guard flags.

### Negative / Trade-offs
* Developers and AI agents must remember to use `debouncedClickable` instead of the standard Compose `clickable` modifier.

## References
* [`docs/design-system/horizon-narrative-design.md`](../../design-system/horizon-narrative-design.md)
* [`docs/design-system/ux-guidelines.md`](../../design-system/ux-guidelines.md)
