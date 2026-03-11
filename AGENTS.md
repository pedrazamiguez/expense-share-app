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
- **Hot flows:** Use `stateIn(scope, SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME), initial)`. The constant lives in `core/common/.../AppConstants.kt` (5000ms). Never hardcode.
- **UiText pattern:** ViewModels emit `UiText.StringResource(R.string.x)` — resolved to String in Feature via `asString(context)`.
- **Formatting belongs in Mappers,** not ViewModels. Mappers receive `LocaleProvider`. See `GroupUiMapperImpl`, `ProfileUiMapperImpl`.

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

