# ADR-0004: MVI Pattern, Stateless Screens, and Handler Decomposition

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** presentation, mvi, compose, uistate, viewmodel, handlers

---

## Context and Problem Statement

Jetpack Compose UI development can easily degrade into tangled states, duplicate event emissions, and hard-to-preview screens if ViewModels, navigation controllers, or context objects are passed deep into composable trees. Additionally, large complex forms (like adding expenses with multi-currency splits and add-ons) can cause ViewModels to balloon into unmaintainable god-classes exceeding 1,000 lines.

How do we structure UI state, user events, side-effects, and screen decomposition to guarantee previewability, testability, and strict file size limits (<600 lines)?

## Decision Drivers

* Strict Unidirectional Data Flow (UDF).
* Frictionless Compose `@Preview` support across dark/light themes and locales without DI or mocks.
* Clear distinction between persistent UI state and one-shot ephemeral side effects.
* Strict compliance with the 600-line source file limit enforced by Konsist.

## Considered Options

1. **MVVM with Multiple StateFlows & Direct Method Calls:** ViewModel exposes multiple individual primitive flows and methods (`onTitleChanged()`, `onSubmit()`). Leads to state synchronization races and difficult event tracing.
2. **Standard MVI in Monolithic ViewModels:** Clean triad, but complex screens produce enormous ViewModel files that violate architecture limits.
3. **MVI Triad + Feature/Screen Separation + Two-Tier Handler Delegation (Chosen):** Single immutable `UiState`, typed `UiEvent` sealed interface, ephemeral `UiAction` channel, stateless `*Screen` composables, stateful `*Feature` orchestrators, and two-tier Handler/Delegate decomposition.

## Decision Outcome

Chosen option: **Option 3 (MVI Triad + Feature/Screen Separation + Two-Tier Handler Delegation)**.

### Architectural Rules
1. **The MVI Triad:**
   - **`UiState`:** Immutable data class holding persistent screen state.
   - **`UiEvent`:** Sealed interface representing user intents dispatched to `viewModel.onEvent(event)`.
   - **`UiAction`:** Ephemeral one-shot side-effects (`SharedFlow` / `Channel`) consumed once (e.g. snackbar/top-pill notifications, back navigation). Never put one-shot flags in `UiState`.
2. **Feature vs. Screen Pattern:**
   - `*Screen` is 100% stateless: receives `UiState` and emits lambda callbacks. Exactly one top-level Composable per file matching the file name.
   - `*Feature` is stateful: injects ViewModel, collects flows with lifecycle, consumes `LocalRootNavController`/`LocalTabNavController`, and hoists to `FeatureScaffold`.
3. **Two-Tier Decomposition for Complex Features:**
   - **Tier 1 (ViewModel → Event Handlers):** ViewModels with >5 event categories or >200 lines delegate events to cohesive `*EventHandler` classes.
   - **Tier 2 (EventHandler → Delegates):** Event Handlers approaching 600 lines delegate calculation logic to `*Delegate` classes.
4. **File Size Hard Limit:** All production Kotlin source files must strictly remain under 600 lines (enforced by Konsist).

## Consequences

### Positive
* Highly predictable state changes with single-direction data flow.
* Seamless Compose Previews without mocking ViewModels or DI containers.
* Zero one-shot replay bugs on configuration changes (rotation/navigation).
* Keeps source files modular, focused, and under the 600-line architectural ceiling.

### Negative / Trade-offs
* Requires defining explicit boilerplate data classes and sealed interfaces for state and events.
* Requires manual wiring of handlers in Koin `viewModel { }` blocks for complex multi-handler features.

## References
* [`docs/architecture/patterns/mvi-and-stateless-screens.md`](../patterns/mvi-and-stateless-screens.md)
* [`docs/design-system/compose-previews.md`](../../design-system/compose-previews.md)
* [`docs/architecture/adrs/0003-clean-architecture.md`](0003-clean-architecture.md)
