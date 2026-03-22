# AGENTS.md — ExpenseShareApp

## Project Overview

Kotlin Android app (Jetpack Compose, Material 3) for shared travel expenses. Multi-module Clean Architecture, offline-first with Room + Firestore, Koin DI. See `.github/copilot-instructions.md` for the full technical manifesto.

## Module Structure & Visibility Rules

```
:app              → Wires DI only. Sees everything.
:core:common      → Constants, UiText, providers (LocaleProvider, ResourceProvider)
:core:design-system → UI components, Routes, NavigationProvider, ScreenUiProvider, preview utils
:domain           → Pure Kotlin: models, repository interfaces, use cases, domain services
:data             → Repository implementations (offline-first)
:data:local       → Room DAOs, entities, DataStore
:data:firebase    → Firestore/Auth cloud data sources
:data:remote      → Retrofit (currency API)
:features:*       → Independent feature modules (UI + ViewModels)
```

**Strict:** Features cannot see other features or `:data`. Features only depend on `:domain` interfaces and `:core`.

## Architecture Constraints

- **ViewModels NEVER depend on Repositories.** Only inject UseCases, Mappers, and Domain Services.
- **ViewModels NEVER inject** `Context`, `LocaleProvider`, or other ViewModels.
- **Feature vs Screen pattern:** `*Feature` (orchestrator composable) holds ViewModel, collects flows, consumes `LocalSnackbarController`/`LocalTabNavController`. `*Screen` is stateless — takes `UiState` + event lambdas only. See `features/profile/src/main/kotlin/.../ProfileFeature.kt` and `ProfileScreen.kt`.
- **MVI triad per screen:** `UiState` (data class, `ImmutableList`), `UiEvent` (sealed interface), `UiAction` (side-effects via `Channel`/`SharedFlow`). Never put one-shot events in `UiState`.
- **Hot flows:** Use `stateIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME, replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION), initial)`. The constants live in `core/common/.../AppConstants.kt` (5000ms / 0ms). Never hardcode. The `FLOW_REPLAY_EXPIRATION = 0` resets the replay cache to `initialValue` after the upstream stops, preventing stale-state flashes on tab re-entry.
- **UiText pattern:** ViewModels emit `UiText.StringResource(R.string.x)` — resolved to String in Feature via `asString(context)`.
- **Formatting belongs in Mappers,** not ViewModels **and not Domain Services**. Mappers receive `LocaleProvider`. See `GroupUiMapperImpl`, `ProfileUiMapperImpl`. Domain Services must NEVER contain `formatShareForInput()`, `formatAmountForDisplay()`, or any human-readable formatting method.
- **Decimal precision:** ALL decimal math in Domain Services, Repositories, and Use Cases MUST use `BigDecimal` with explicit `RoundingMode` and `scale` — NEVER `Double` or `Float`. All domain model fields that represent decimal values (e.g., `Subunit.memberShares`, exchange rates) use `BigDecimal`. Boundary serialization at the Firestore document layer uses `String` (via `toPlainString()` / safe `toBigDecimalOrNull()`) to avoid IEEE 754 floating-point precision loss that `Double` would introduce. See `SplitPreviewService` as reference.
- **Handler delegation:** When a ViewModel's `onEvent()` handles >5 event categories or exceeds ~200 lines, extract logic into plain **Event Handler** classes (NOT ViewModels) that receive `MutableStateFlow<UiState>`, `MutableSharedFlow<UiAction>`, and `CoroutineScope` via `bind()`. See `AddExpenseEventHandler`, `ConfigEventHandler`, `SplitEventHandler` in `:features:expenses`.
- **Bottom padding:** All tab screens (hosted via `ScreenUiProvider`) MUST read `LocalBottomPadding.current` and apply it as bottom content padding to lists, FABs, and bottom-anchored buttons. Never hardcode `padding(bottom = 80.dp)`. See `ExpensesScreen`, `GroupsScreen`, `BalancesScreen`.

## Navigation

- Routes are `const val` in `core/design-system/.../Routes.kt`.
- Two nav controllers: `LocalRootNavController` (full-screen flows) and `LocalTabNavController` (within bottom tabs). Consumed via CompositionLocals in Feature layer only.
- Snackbars: `LocalSnackbarController` — never use `Scaffold(snackbarHost=...)` in features.
- Features register as bottom tabs via `NavigationProvider` interface + Koin `bind`. See `GroupsNavigationProviderImpl`.
- Tab screens define TopBar/FAB via `ScreenUiProvider` implementations (not their own Scaffold).

## Offline-First Data Flow

1. **Read:** UI observes Room Flow only. Repository subscribes to Firestore snapshots via `onStart` and reconciles Room using `@Transaction` upsert + selective delete.
2. **Write:** Save to Room first (instant UI update), then `syncScope.launch { cloudDataSource.upsert(...) }`.
3. **IDs:** Always generate `UUID` locally — never use Firestore `.add()`.
4. **Cloud subscriptions:** Track as `Job` and cancel before re-launching to prevent duplicate listeners. See `GroupRepositoryImpl`, `ExpenseRepositoryImpl`.
5. **Inject `CoroutineDispatcher`** (default `Dispatchers.IO`) for testability.

## DI Pattern (Koin)

Each feature has a triple of modules wired in `app/.../FeatureModuleAggregations.kt`:
```
groupsDomainModule + groupsDataModule + groupsUiModule → groupsFeatureModules
```
- UI modules declare: ViewModel, Mapper, `NavigationProvider` (factory + bind), `ScreenUiProvider` (single + bind).
- See `features/groups/.../GroupsUiModule.kt` and `features/profile/.../ProfileUiModule.kt` as templates.

## Testing

- **Framework:** Unit tests are primarily JUnit 5 + MockK. Some legacy/Robolectric unit tests still use JUnit 4 and run via the JUnit Vintage engine. Android instrumentation tests use `AndroidJUnit4`.
- **Assertions:** NEVER use Kotlin's `assert()` — it's a no-op on Android. ALWAYS use JUnit `Assert.assertTrue(...)`, `Assert.assertEquals(...)`, etc.
- **Repository tests:** Inject `StandardTestDispatcher()` into both the repo and `runTest(testDispatcher)`. Call `advanceUntilIdle()` before assertions. See `data/src/test/.../ContributionRepositoryImplTest.kt`.
- **ViewModel tests:** Test via `onEvent()` inputs and StateFlow/SharedFlow outputs.
- **Mapper tests:** Use `LocaleProvider` fakes with fixed `Locale`. See `features/groups/src/test/.../GroupUiMapperImplTest.kt`.
- **Instrumentation tests:** Compose UI + navigation tests live in `app/src/androidTest/`. Custom `TestRunner` + `TestApp` bypass the production Koin graph. Tests use `KoinApplication` wrapper to inject mocks per-test. See `AppNavHostTest.kt` and `MainScreenTest.kt`.
- **Instrumentation test pattern:** `AppNavHost` uses `getKoin()` (composable-scoped) instead of `GlobalContext.get()`, so tests can wrap it in `KoinApplication { modules(testModule) }`.
- **Smoke tests:** Stateless Screen composables (LoginScreen, OnboardingScreen, ProfileScreen) are tested directly with different UiState configurations — no ViewModel or Koin needed. See `app/src/androidTest/.../screens/`.
- **Shared test helpers:** `FakeNavigationProvider` in `app/src/androidTest/.../helpers/` provides a minimal `NavigationProvider` that renders plain `Text("Content: $label")`, avoiding all feature dependencies.
- **Reusable test modules:** `TestModules.kt` in `app/src/androidTest/.../di/` provides `createAppNavHostTestModule()` with configurable auth/onboarding flows.
- Run unit tests: `./gradlew test`
- Run instrumentation tests: `./gradlew connectedAndroidTest`
- **CI:** Instrumentation tests run via `.github/workflows/instrumentation-tests.yml` — triggers on `main` push and `workflow_dispatch` (manual). Uses `reactivecircus/android-emulator-runner@v2` with API 30 (configurable).

## Compose Previews

- Wrap in `PreviewThemeWrapper`. Use `@PreviewLocales` (EN/ES), `@PreviewThemes` (Light/Dark), or `@PreviewComplete` (all 4).
- Use `MappedPreview` + `*PreviewHelper` in `src/debug` for domain→mapper→UiModel previews.
- Preview utilities live in `core/design-system/src/debug/.../preview/`.

## Build & Run

- JDK 21, Android SDK 36, min SDK 26.
- Place `google-services.json` in `app/`. Set `OPEN_EXCHANGE_RATES_APP_ID` in `local.properties`.
- Version managed in `version.properties` (major.minor.patch + snapshot flag).
- `./gradlew test` — unit tests. `./gradlew connectedAndroidTest` — UI tests.

## Static Analysis & Code Quality

- **Detekt** (code quality/complexity) and **Ktlint** (formatting) are configured in `build.gradle.kts` for all subprojects.
- **CodeQL** (security) runs separately via `.github/workflows/codeql.yml`. All three tools coexist — different concerns, different SARIF categories.
- Detekt config lives at `config/detekt/detekt.yml`. Ktlint rules are in `.editorconfig`.
- CI runs static analysis via `.github/workflows/static-analysis.yml` — parallel to and independent of `build-and-test.yml`.
- Detekt uses `ignoreFailures = true` locally; gating is done by GitHub Code Scanning's "Code scanning results" check (only new alerts block PRs).
- Pre-commit hook runs **ktlint only** (fast). Detekt runs in CI only.
- New code must not introduce new detekt findings. Formatting must comply with ktlint / `.editorconfig`.
- See `wiki/code-quality-and-static-analysis.md` for full details.

## AI Agent Behavior Rules (CRITICAL)

- **Read before you act:** Before ANY implementation, read `.github/copilot-instructions.md`, `AGENTS.md`, and all relevant `wiki/*.md` articles. Study existing reference implementations in the codebase.
- **NEVER push code** to any remote branch without explicit user permission.
- **NEVER create Pull Requests** without explicit user permission and confirmation of branch naming convention (see `wiki/branching-versioning-release-strategy.md`), target branch, and PR format.
- **NEVER comment on GitHub issues or PRs** without the user explicitly requesting it.
- **NEVER merge PRs or close issues** autonomously.
- **Compliance checklist before generating code:** (1) ViewModels only inject UseCases/Mappers/Services? (2) Formatting only in Mappers? (3) BigDecimal for all decimal math? (4) Handler delegation for >5 events? (5) `LocalBottomPadding` for tab screens? (6) Feature/Screen split correct? (7) MVI triad complete? (8) Hot flows with `AppConstants.FLOW_RETENTION_TIME` and `AppConstants.FLOW_REPLAY_EXPIRATION`? (9) Offline-first Room-first reads? (10) `ImmutableList` in UiState?

