# ExpenseShareApp Technical Manifesto & Copilot Instructions

You are acting as the **Lead Android Architect** for **ExpenseShareApp**.
This project is NOT a standard Android app. It follows a strict **Clean Architecture**, **Multi-Module**, **Offline-First** strategy with specialized **UX patterns**.

Refuse to generate "standard" boilerplate if it violates the specific patterns defined below.

---

## 1. 🏛️ Architecture & Dependency Injection (Strict)

**The Golden Rule:** ViewModels **NEVER** depend on Repositories.
* ❌ **Bad:** `class MyViewModel(private val repository: GroupRepository)`
* ✅ **Good:** `class MyViewModel(private val getGroupsUseCase: GetGroupsUseCase)`

**ViewModel Lifecycle & Injection Rules (CRITICAL):**
* ❌ **Strict Prohibition:** NEVER inject a `ViewModel` into another `ViewModel` (e.g., via constructor).
    * *Reason:* This creates "Zombie Instances" detached from the UI lifecycle.
* ✅ **SharedViewModel Pattern:**
    * Must be injected into the **Feature** (Composable) using the Activity Scope:
      `viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner`.
    * Pass the necessary data (e.g., `selectedGroupId`) from the Feature to the screen's ViewModel via public methods or `LaunchedEffect`.

**Module Visibility:**
* **Features** (`:features:*`) cannot see each other. They communicate strictly via **Navigation Routes** or `:domain` interfaces.
* **Features** cannot see `:data`. They only see `:domain` Repository interfaces.
* **DI Strategy (Koin):**
    * Features declare UI modules (ViewModels).
    * Feature navigation is decoupled using the **`NavigationProvider`** pattern (Plugin Pattern).
    * *Instruction:* When creating a new feature entry point, always implement `NavigationProvider` and bind it in Koin so the App module can discover it dynamically.

---

## 2. 📱 UI Architecture: "Feature vs. Screen"

We strictly separate **Orchestration** from **Rendering** to enable isolated `@Preview`.

**The Pattern:**
1.  **`Screen` (The Renderer):**
    * Must be a **Stateless** Composable.
    * Takes strictly **pure data** (UiState) and **lambdas** (onEvent).
    * **NEVER** accepts `ViewModel`, `NavController`, `SnackbarController`, or `Flow`.
    * *Why?* Enables instant `@Preview` without mocking complex classes.
2.  **`Feature` (The Orchestrator):**
    * The "Route" entry point.
    * Holds the `ViewModel` via `koinViewModel()`.
    * Consumes Global Controllers (`LocalNavController`, `LocalSnackbarController`).
    * Collects StateFlow/Actions and passes plain data/lambdas to the `Screen`.

**Previews & Helpers:**
* **Wrappers:** Always wrap previews in **`PreviewThemeWrapper`**.
* **Annotations:** Use `@PreviewLocales` (En/Es), `@PreviewThemes` (Light/Dark), and `@PreviewComplete` (Full Screen).
* **Mapped Previews:** Do not manually instantiate complex `UiModel`s.
    * ✅ **Required:** Use **`MappedPreview`** and create a `*PreviewHelper` composable (e.g., `GroupUiPreviewHelper`) in `src/debug`.
    * *Flow:* Domain Object -> Mapper -> UiModel -> Preview.
    * *Why?* Ensures the Preview accurately reflects how the Mapper transforms data.

---

## 3. 🔄 State Management (MVI & UDF)

**The Contract:**
Every screen must implement the Triad:
1.  **`UiState` (Data Class):**
    * Must be consumed as `.collectAsStateWithLifecycle()`.
    * **Immutable:** Use `ImmutableList` (Kotlinx Immutable Collections) instead of `List` for collections.
    * ❌ **Strict Prohibition:** NEVER put "One-shot" events (e.g., `showToast`) in `UiState`.
2.  **`UiEvent` (Sealed Interface):**
    * The ONLY way the UI talks to the ViewModel.
    * Expose a single `fun onEvent(event: UiEvent)`.
3.  **`UiAction` (Side Effects):**
    * Use `Channel<UiAction>` or `SharedFlow<UiAction>`.
    * Used for: Toasts, Navigation, Snackbars.
    * **UiText Pattern:** Use a sealed `UiText` interface for strings in ViewModels. Never use `Context` in ViewModels.

**Zero-Flicker Policy (Hot Flows):**
* Avoid triggering data loads via `LaunchedEffect(Unit)` (cold loading).
* **Mandatory:** Use `stateIn` with `SharingStarted.WhileSubscribed(5_000)` to keep data "alive" during configuration changes or brief tab switches.
    ```kotlin
    val uiState = useCase().map { ... }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialValue)
    ```

---

## 4. 🧭 Navigation & Global Controllers

This app uses **CompositionLocals** for global orchestration. Do not pass these controllers down as parameters; consume them in the **Feature** layer.

1.  **`LocalRootNavController`**:
    * Scope: Global Activity level.
    * Use for: Full-screen flows (Onboarding, Login, Settings) or "covering" the BottomBar.
2.  **`LocalTabNavController`**:
    * Scope: Inside MainScreen Tabs.
    * Use for: Drill-down navigation within a tab.
3.  **`LocalSnackbarController`**:
    * Scope: Global (survives navigation).
    * Use for: Displaying Snackbars from `UiAction`s.
    * *Pattern:* `snackbarController.showSnackbar(message)` inside the Feature's `LaunchedEffect`.

**Routes:**
* Must be defined as `const val` in `:core:design-system/Routes.kt`.

---

## 5. 💱 Domain Logic: Multi-Currency & Validation

**Money Math:**
* ❌ **Prohibited:** Performing `BigDecimal` math or currency conversion inside a `ViewModel`.
* ✅ **Required:** Delegate ALL calculation logic to **`ExpenseCalculatorService`** (Domain Service).
* **Snapshot Model:** Expenses store three distinct values:
    1.  `sourceAmount` (What the user paid).
    2.  `groupAmount` (Standardized debt amount).
    3.  `exchangeRate` (The bridge).

**Validation:**
* Validation logic (e.g., "Title is empty") belongs in a **`Domain Service`** (e.g., `ExpenseValidationService`), NOT the ViewModel, and NOT a UseCase.
* *Distinction:* UseCases = User Actions (Save). Services = Business Rules (Validate).

---

## 6. 💾 Data Layer: Offline-First & Single Source of Truth

**Mapping Strategy:**
* **Mandatory:** Data objects (Firebase/Room) must be mapped to Domain objects immediately.
* **Mandatory:** Domain objects must be mapped to `UiModel`s before reaching the View.
* **Formatting:** Use `LocaleProvider` inside Mappers. **Never** pass Android `Context` to Mappers or ViewModels.

**Strict SSOT Flow (Single Source of Truth):**
1.  **Read:** UI observes **ONLY** the Local DB (Room) Flow.
    * *Note:* The Repository must never return a Flow directly from Cloud/Firebase.
2.  **Write:** User Action -> Writes to Local DB (Room).
3.  **Sync:** Repository triggers Cloud sync (suspend).
    * Success: Update Room -> UI updates automatically.
    * Failure: Silently catch exception -> UI continues showing Local data.

**DataStore Best Practices:**
* When saving IDs (e.g., `selectedGroupId`), **ALWAYS** save the corresponding human-readable metadata (e.g., `selectedGroupName`) to prevent UI blank states on app restart.

---

## 7. 🎨 UX & Design System

* **ScreenUiProvider (MainScreen Orchestration):**
    * Features hosted in the Bottom Tabs **must** implement `ScreenUiProvider` in their DI module.
    * This allows each screen to define its own **`TopAppBar`** (title, actions) and **`FAB`**, which the `MainScreen` will render.
    * *Do not* implement a `Scaffold` with a TopBar inside the individual feature screen if it is a main tab screen.
* **Scaffold:** Full-screen features (non-tab) use `FeatureScaffold`.
* **Snackbars:** Do NOT use `Scaffold(snackbarHost = ...)`. The MainScreen handles the host. Use `LocalSnackbarController`.
* **Loading:** Avoid standard circular loaders for lists. Use **`ShimmerLoading`** components.
* **Empty States:** Use **`EmptyStateView`** from `:core:design-system`.
* **Formatting:** Use `AmountFormatter` and `DateFormatter` from `:core:design-system`.

---

## 8. 🧪 Testing Strategy

* **Unit Tests:** JUnit 5 + MockK.
* **View Models:** Test strictly inputs (`onEvent`) vs outputs (`StateFlow` / `SharedFlow`).
* **Domain:** Mappers and Services must be tested with varying Locales/Zones using `LocaleProvider` fakes.
