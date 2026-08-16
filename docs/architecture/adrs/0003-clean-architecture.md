# ADR-0003: Multi-Module Clean Architecture & Visibility Rules

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** architecture, modularity, clean-architecture, gradle, konsist

---

## Context and Problem Statement

As SplitTrip grows in functionality (groups, expenses, balances, contributions, withdrawals, subunits, settlements), maintaining loose coupling, fast incremental compilation, and clear ownership boundaries becomes paramount. Monolithic architectures or loose module visibility lead to circular dependencies, leaky abstractions, and brittle refactorings.

How should the codebase be modularized to guarantee strict separation of concerns, testability, and isolated compile-time feature boundaries?

## Decision Drivers

* Strict isolation between business logic, data persistence, and UI rendering.
* Independent compilation units for fast build times and parallel CI checks.
* Prevention of accidental cross-feature dependencies.
* Enforceability via automated architectural linting (Konsist).

## Considered Options

1. **Monolithic App Module:** Simple setup, but poor build performance, zero boundary enforcement, and severe coupling.
2. **Layer-by-Feature (Horizontal):** Few large modules (`:feature`, `:domain`, `:data`), but lacks isolation between independent features.
3. **Multi-Module Clean Architecture with Directional Visibility (Chosen):** Granular vertical feature modules interacting with pure Kotlin `:domain` and shared `:core` modules, wired exclusively in `:app`.

## Decision Outcome

Chosen option: **Option 3 (Multi-Module Clean Architecture with Directional Visibility)**.

### Module Hierarchy & Dependency Graph

```
:app                    → Wires DI only. Sees everything.
:core:common            → Constants, UiText, providers (LocaleProvider, ResourceProvider)
:core:design-system     → UI components, Routes, NavigationProvider, TabGraphContributor, ScreenUiProvider
:domain                 → Pure Kotlin: models, repository interfaces, use cases, domain services
:data                   → Repository implementations (offline-first coordination)
:data:local             → Room DAOs, entities, DataStore
:data:firebase          → Firestore/Auth cloud data sources
:data:remote            → Retrofit (currency API)
:features:*             → Isolated feature modules (groups, expenses, balances, contributions, etc.)
```

### Strict Visibility & Architectural Rules
1. **Features Depend Only on Domain & Core:** Feature modules (`:features:*`) must NEVER depend on `:data`, other `:features:*`, or Android framework data layers.
2. **Domain is Pure Kotlin:** `:domain` contains zero Android SDK dependencies (`android.*`).
3. **Data Layer Implements Domain Contracts:** `:data` implements repository interfaces defined in `:domain`.
4. **App Module Wires Dependency Graph:** Only `:app` depends on all modules to assemble the Koin dependency injection graph.
5. **No Fully Qualified Names (No FQN):** All classes, interfaces, and types must be explicitly imported at the top of source files.
6. **Konsist Enforcement:** All visibility and naming rules are enforced automatically by Konsist tests in `:konsist-tests`.

## Consequences

### Positive
* High maintainability and deterministic module boundaries.
* Substantially faster incremental build times and build cache hit rates.
* Pure domain logic can be unit-tested rapidly without Robolectric or Android emulators.
* Features can be added, refactored, or replaced in total isolation.

### Negative / Trade-offs
* Requires Gradle boilerplate across multiple `build.gradle.kts` files (mitigated via Version Catalogs and convention plugins).
* Inter-feature navigation requires decoupling patterns (`TabGraphContributor`, `ScreenUiProvider`, string-based routes).

## References
* [`docs/architecture/patterns/modularity-and-visibility.md`](../patterns/modularity-and-visibility.md)
* [`docs/architecture/patterns/navigation-and-routing.md`](../patterns/navigation-and-routing.md)
* [`docs/engineering/quality-and-static-analysis.md`](../../engineering/quality-and-static-analysis.md)
