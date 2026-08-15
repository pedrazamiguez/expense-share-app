# ADR-0006: Koin Dependency Injection Standards & Variable Naming

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** di, koin, architecture, conventions, konsist

---

## Context and Problem Statement

Dependency Injection in multi-module Kotlin Android applications can suffer from runtime resolution ambiguity, confusing constructor injections, and unreadable DI definitions if variable naming is inconsistent or overly concise (e.g. `val mapper = get()`).

How do we standardize Koin DI definitions, module aggregations, and variable declarations to ensure clarity, type-safety, and strict architectural enforcement?

## Decision Drivers

* High readability and self-documenting DI module definitions.
* Prevention of type resolution collisions in Koin factories and viewModels.
* Consistent module aggregation patterns across domain, data, and presentation layers.
* Enforceability via Konsist architecture linting.

## Considered Options

1. **Short / Inferred Variable Names in DI (`val mapper = get()`):** Fast to write, but creates ambiguity when multiple mappers or services are injected into handlers.
2. **Annotation-based DI (Hilt / Dagger):** Strong compile-time validation, but introduces significant annotation processing overhead (KSP/kapt) and rigid module dependencies.
3. **Koin DI with Full Class camelCase Variable Naming & Layer Aggregations (Chosen):** Pure Kotlin DSL, fast compilation, explicit variable naming, and structured feature module aggregations.

## Decision Outcome

Chosen option: **Option 3 (Koin DI with Full Class camelCase Variable Naming & Layer Aggregations)**.

### Architectural Rules
1. **Full Class Name in camelCase:** Variables inside `viewModel { }` and `factory { }` blocks must use the full class name in camelCase.
   - ✅ `val addExpenseUiMapper = get<AddExpenseUiMapper>()`
   - ✅ `val addExpenseOptionsUiMapper = get<AddExpenseOptionsUiMapper>()`
   - ❌ `val mapper = get()`, `val optionsMapper = get()`
2. **Explicit Constructor Argument Names:** Constructor arguments passed to handlers, view models, or mappers must use descriptive parameter names matching the injected type.
   - ✅ `addCashWithdrawalUiMapper = cashWithdrawalUiMapper`
   - ❌ `mapper = cashWithdrawalUiMapper`
3. **Structured Feature Module Aggregation:**
   - Each feature defines granular layer modules (`*DomainModule`, `*DataModule`, `*UiModule`) aggregated into `*FeatureModules` in `app/.../FeatureModuleAggregations.kt`.
4. **Scope Isolation:**
   - Tab features register `NavigationProvider` (`factory { ... } bind NavigationProvider::class`).
   - Non-tab sub-flows register `TabGraphContributor` (`factory { ... } bind TabGraphContributor::class`).
   - Screen chrome registers `ScreenUiProvider` (`single { ... } bind ScreenUiProvider::class`).

## Consequences

### Positive
* Crystal clear DI graph with zero ambiguity during code reviews or refactorings.
* Consistent patterns across all feature modules.
* Direct validation via Konsist tests.

### Negative / Trade-offs
* Requires slightly more verbose DI declarations.

## References
* [`docs/architecture/patterns/modularity-and-visibility.md`](../patterns/modularity-and-visibility.md)
* [`docs/architecture/patterns/navigation-and-routing.md`](../patterns/navigation-and-routing.md)
* [`docs/architecture/adrs/0003-clean-architecture.md`](0003-clean-architecture.md)
