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

**ViewModel Dependencies (STRICT):**
* ✅ **ONLY inject:** UseCases, Mappers, and Domain Services (e.g., `ExpenseCalculatorService`)
* ❌ **NEVER inject:** 
    * `LocaleProvider` - This belongs in Mappers for formatting/mapping logic
    * `Context` - Use `UiText` pattern instead
    * Repositories - Always use UseCases
    * Formatters directly - Use Mappers which wrap formatters
* **Rationale:** ViewModels manage state and orchestrate use cases. Data transformation, formatting, and locale-aware operations are mapping concerns, not state management concerns.

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
* **Mappers Handle Formatting:** Even when using extension functions from `:core:design-system` (like `formatNumberForDisplay`), the **Mapper** must call them, NOT the ViewModel.
    * ✅ **Correct:** Mapper receives `LocaleProvider`, calls `value.formatNumberForDisplay(locale = localeProvider.getCurrentLocale())`
    * ❌ **Wrong:** ViewModel receives `LocaleProvider` and calls formatting functions directly
    * *Reason:* Formatting is a mapping/transformation concern, not a state management concern.

**Strict SSOT Flow (Single Source of Truth):**
1.  **Read:** UI observes **ONLY** the Local DB (Room) Flow.
    * *Note:* The Repository must never return a Flow directly from Cloud/Firebase.
2.  **Write:** User Action -> Writes to Local DB (Room).
3.  **Sync:** Repository triggers Cloud sync (suspend).
    * Success: Update Room -> UI updates automatically.
    * Failure: Silently catch exception -> UI continues showing Local data.

**DataStore Best Practices:**
* When saving IDs (e.g., `selectedGroupId`), **ALWAYS** save the corresponding human-readable metadata (e.g., `selectedGroupName`) to prevent UI blank states on app restart.

### 6.1 🛑 The "True Offline" Write Protocol

We use a strictly **"Offline-First"** approach. The UI only observes the Local DB. The Cloud is a replication target, not the source of truth for the UI.

When creating new data (Expenses, Groups, etc.), you **MUST** follow this exact order to prevent UI jumping, sorting issues, and duplicates.

1.  **Local ID Generation:**
    * **NEVER** let Firestore generate the ID (e.g., do not use `.add()`).
    * **ALWAYS** generate a `UUID` locally in the Repository or UseCase.
    * *Reason:* We need the ID immediately for the Local DB to prevent duplicates during sync.
2.  **Local Metadata Generation:**
    * **Timestamps:** Generate `createdAt = System.currentTimeMillis()` locally.
        * ❌ **Bad:** Relying on Firestore `@ServerTimestamp` or leaving it `0`.
        * *Consequence:* Items appear at the bottom of the list or disappear until a sync happens.
    * **User Attribution:** Inject `AuthenticationService` into the Repository and set `createdBy = currentUserId` locally.
        * ❌ **Bad:** Waiting for cloud functions to set the user ID.
3.  **Repository Write Order:**
    1.  **Save to Room (Local) FIRST.** -> *UI updates instantly.*
    2.  **Launch Background Job.** -> *Sync to Cloud.*
4.  **Cloud Operation:**
    * Use `.document(localId).set(data)` (Upsert).
    * **NEVER** use `.collection(...).add(data)`.

### 6.2 🔄 The Sync Protocol (Read)
When fetching data from the cloud:
1.  **Upsert Strategy:** Use `OnConflictStrategy.REPLACE` in Room DAOs.
2.  **Race Condition Protection:**
    * **NEVER** `deleteAll()` before inserting synced data. This wipes out unsynced local changes.
    * Only insert/update the specific items returned from the cloud.

### 6.3 DataStore Best Practices
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

### 8.1 🧵 Coroutine Testing (CRITICAL - Prevents Flaky Tests)

When testing classes that launch background coroutines (e.g., Repositories with `syncScope.launch {}`), you **MUST** inject the `CoroutineDispatcher` to ensure deterministic test behavior.

**The Problem:**
Tests using `advanceUntilIdle()` only control coroutines running on the **test dispatcher**. If your class creates its own `CoroutineScope(Dispatchers.IO)`, those coroutines run on a **real dispatcher** that the test cannot control. This causes:
* ✅ Tests pass locally (faster machine, lucky timing)
* ❌ Tests fail on CI/GitHub Actions (slower, different scheduling)

**❌ BAD - Hardcoded Dispatcher (Flaky Tests):**
```kotlin
class GroupRepositoryImpl(
    private val cloudDataSource: CloudGroupDataSource,
    private val localDataSource: LocalGroupDataSource
) : GroupRepository {
    // ❌ NOT testable - test cannot control this scope
    private val syncScope = CoroutineScope(Dispatchers.IO)
    
    override suspend fun deleteGroup(groupId: String) {
        localDataSource.deleteGroup(groupId)
        syncScope.launch {
            cloudDataSource.deleteGroup(groupId) // Runs on real IO dispatcher
        }
    }
}

// ❌ This test is FLAKY - advanceUntilIdle() has no effect on Dispatchers.IO
@Test
fun `syncs to cloud in background`() = runTest {
    repository.deleteGroup("123")
    advanceUntilIdle() // Does NOT wait for syncScope coroutines!
    coVerify { cloudDataSource.deleteGroup("123") } // May fail randomly
}
```

**✅ GOOD - Injected Dispatcher (Deterministic Tests):**
```kotlin
class GroupRepositoryImpl(
    private val cloudDataSource: CloudGroupDataSource,
    private val localDataSource: LocalGroupDataSource,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO  // ✅ Injectable with default
) : GroupRepository {
    private val syncScope = CoroutineScope(ioDispatcher)  // ✅ Uses injected dispatcher
    
    override suspend fun deleteGroup(groupId: String) {
        localDataSource.deleteGroup(groupId)
        syncScope.launch {
            cloudDataSource.deleteGroup(groupId)
        }
    }
}

// ✅ Test provides StandardTestDispatcher - advanceUntilIdle() now works!
@OptIn(ExperimentalCoroutinesApi::class)
class GroupRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeEach
    fun setUp() {
        repository = GroupRepositoryImpl(
            cloudDataSource = mockk(relaxed = true),
            localDataSource = mockk(relaxed = true),
            ioDispatcher = testDispatcher  // ✅ Inject test dispatcher
        )
    }
    
    @Test
    fun `syncs to cloud in background`() = runTest(testDispatcher) {  // ✅ Same dispatcher
        repository.deleteGroup("123")
        advanceUntilIdle()  // ✅ Now correctly waits for all coroutines
        coVerify { cloudDataSource.deleteGroup("123") }  // ✅ Deterministic
    }
}
```

**Key Rules:**
1. **Always inject `CoroutineDispatcher`** into classes that launch background coroutines.
2. **Provide a default** (`= Dispatchers.IO`) so production code doesn't need to specify it.
3. **Use `StandardTestDispatcher()`** in tests and pass it to both the class and `runTest()`.
4. **Call `runTest(testDispatcher)`** - the dispatcher must match what's used in the class.
5. **Call `advanceUntilIdle()`** before assertions to ensure background work completes.

