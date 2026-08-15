# MVI & Stateless Screens Architecture

This document describes SplitTrip's Unidirectional Data Flow (UDF) pattern—specifically a variation of MVI (Model-View-Intent)—to ensure a clean separation of concerns, predictability, and testability across Jetpack Compose UI.

---

## 1. The Core Triad: State, Events, and Side Effects

We strictly distinguish between **State** (what the UI shows), **Events** (what the user does), and **Side Effects** (one-off occurrences).

### 1.1 UI State (`StateFlow<UiState>`)
* **Definition:** Represents the persistent "snapshot" of the screen at any given moment.
* **Implementation:** `StateFlow<UiState>` in the ViewModel, observed via `collectAsStateWithLifecycle()`.
* **Characteristics:**
  * **Sticky:** It always has a value. When a view subscribes, it immediately receives the latest state.
  * **Persistent:** It survives configuration changes (such as screen rotations).
  * **Immutability:** State objects must be immutable (`data class` with `val` fields and `ImmutableList` collections).
* **Examples:** Loading indicators, lists of data (Expenses, Groups), input field text, validation errors (e.g., "Title cannot be empty").

### 1.2 UI Events (`onEvent(UiEvent)`)
* **Definition:** Represents the user's intent or inputs. These are the "Inputs" to the ViewModel.
* **Implementation:** A single public method `fun onEvent(event: UiEvent)` that accepts a sealed interface.
* **Characteristics:**
  * **Decoupled:** The UI does not call specific helper methods (e.g., `submitData()`). Instead, it notifies the ViewModel that an event occurred (e.g., `SubmitButtonClicked`).
  * **Traceable:** Makes debugging and logging easier as every action flowing into the ViewModel is an explicit typed object.
* **Examples:** `TitleChanged`, `SubmitPressed`, `DeleteClicked`.

### 1.3 UI Side Effects / Actions (`SharedFlow<UiAction>` / `Channel`)
* **Definition:** Represents ephemeral, "fire-and-forget" occurrences that should happen exactly once. These are the "One-shot Outputs".
* **Implementation:** `SharedFlow<UiAction>` (configured for no replay) or `Channel<UiAction>`.
* **Characteristics:**
  * **Ephemeral:** These events are not part of the state. They happen and disappear.
  * **Non-Sticky:** If the UI is not observing when the effect is emitted, it is dropped (intentional for UI feedback).
  * **Lifecycle Aware:** They do **not** re-trigger upon screen rotation.
* **Examples:** Showing a top pill notification, navigating to another screen, vibration feedback.

---

## 2. Why This Distinction Matters

### State vs. Side Effects
The most common mistake is putting "One-shot" events into the `UiState`.
* ❌ **Bad Practice:** Adding a `showErrorToast: Boolean` to `UiState`.
  * *Problem:* If the user rotates the screen, the Activity recreates, observes the State again, sees `showErrorToast = true`, and shows the Toast *again*.
* ✅ **Good Practice:** Emitting a `ShowError` action via `SharedFlow`.
  * *Benefit:* The message is consumed once. Recomposition or rotation does not replay old events.

### Events vs. Direct Method Calls
Using `onEvent` enforces a strict API surface. The View acts purely as a dumb renderer that forwards user intents. It prevents the View from knowing *how* the ViewModel processes logic, allowing for easier refactoring and unit testing.

### Code Example: Triad Contract

```kotlin
// State: Persistent data
data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val isLoading: Boolean = false,
    val titleError: String? = null // Validation error is State (visual)
)

// Event: Inputs from the User
sealed interface AddExpenseUiEvent {
    data class TitleChanged(val newTitle: String) : AddExpenseUiEvent
    data object SubmitPressed : AddExpenseUiEvent
}

// Action: One-shot Side Effects
sealed interface AddExpenseUiAction {
    data class ShowError(val message: UiText) : AddExpenseUiAction
    data object NavigateBack : AddExpenseUiAction
}
```

---

## 3. The Feature vs. Screen Pattern

To enable Jetpack Compose `@Preview` and isolate dependencies, we strictly separate the "Logic Orchestration" from the "Pure UI Rendering".

### 3.1 The Stateless `*Screen`
* **Responsibility:** Pure UI rendering.
* **Dependencies:** None. It relies only on primitive types, domain/UI models, and explicit lambda callbacks.
* **State:** Stateless (receives `UiState` as parameters).
* **Events:** Exposes lambdas (e.g., `onCurrencySelected: (String) -> Unit` or `onEvent: (UiEvent) -> Unit`).
* **Benefit:** Can be previewed in Android Studio across themes and locales with zero mocks or DI.

```kotlin
@Composable
fun DefaultCurrencyScreen(
    availableCurrencies: List<Currency>,
    selectedCurrencyCode: String?,
    onCurrencySelected: (String) -> Unit
) {
    // Pure UI rendering with design system components
}
```

### 3.2 The Stateful `*Feature` Orchestrator
* **Responsibility:** Wires ViewModel, observes flows, handles navigation, and sets up scaffolds.
* **Dependencies:** `ViewModel`, `LocalRootNavController`, `LocalTabNavController`, `LocalTopPillController`.
* **State:** Collects `StateFlow` from the ViewModel.
* **Events:** Dispatches user events to ViewModel `onEvent()` and executes UI side-effects from `actions`.
* **Benefit:** Isolates Android/Koin dependencies completely from the presentation layer.

```kotlin
@Composable
fun DefaultCurrencyFeature(
    viewModel: DefaultCurrencyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = LocalRootNavController.current
    val topPillController = LocalTopPillController.current

    // Observe one-shot side-effects
    ObserveAsEvents(viewModel.actions) { action ->
        when (action) {
            is DefaultCurrencyUiAction.NavigateBack -> navController.popBackStack()
            is DefaultCurrencyUiAction.ShowError -> topPillController.showError(action.message)
        }
    }

    FeatureScaffold(currentRoute = Routes.SETTINGS_DEFAULT_CURRENCY) {
        DefaultCurrencyScreen(
            availableCurrencies = uiState.availableCurrencies,
            selectedCurrencyCode = uiState.selectedCurrencyCode,
            onCurrencySelected = { code -> viewModel.onEvent(DefaultCurrencyUiEvent.CurrencySelected(code)) }
        )
    }
}
```

### Rule of Thumb
* Never pass a `ViewModel` into a `Screen`.
* Never pass a `NavController` into a `Screen`.
* Pass them into the `Feature`, which orchestrates the `Screen`.

---

## 4. Handler Delegation & The Delegate Sub-Pattern

To avoid massive ViewModels and respect the **600-line hard limit** enforced by Konsist, complex features use a two-tier decomposition pattern.

### Tier 1: ViewModel → Event Handlers
When a ViewModel's `onEvent()` handles **more than ~5 event categories** or the file exceeds **~200 lines**, logic is extracted into **Event Handler** classes. The ViewModel becomes a thin router.

Handlers are plain classes (NOT ViewModels) that receive `MutableStateFlow<UiState>`, `MutableSharedFlow<UiAction>`, and `CoroutineScope` via a `bind()` method. They are created inside the `viewModel { }` Koin block.

```kotlin
interface MyFeatureEventHandler {
    fun bind(
        stateFlow: MutableStateFlow<MyUiState>,
        actionsFlow: MutableSharedFlow<MyUiAction>,
        scope: CoroutineScope
    )
}

class MyViewModel(
    private val formHandler: FormEventHandler,
    private val submitHandler: SubmitEventHandler
) : ViewModel() {
    init {
        formHandler.bind(_uiState, _actions, viewModelScope)
        submitHandler.bind(_uiState, _actions, viewModelScope)
    }

    fun onEvent(event: MyUiEvent) {
        when (event) {
            is MyUiEvent.NameChanged -> formHandler.handleNameChanged(event.name)
            is MyUiEvent.Submit -> submitHandler.handleSubmit()
        }
    }
}
```

### Tier 2: Event Handler → Delegates
When an Event Handler itself approaches **~600 lines**, cohesive calculation sections are extracted into **Delegate** classes.

| Tier | Class Type | Trigger | Role |
|---|---|---|---|
| **Tier 1** | `*ViewModel` → `*EventHandler` | ViewModel >5 event categories or >200 lines | Thin router delegating events to handlers |
| **Tier 2** | `*EventHandler` → `*Delegate` | Handler >600 lines | Handler delegating cohesive logic to delegates |

#### Delegate Patterns
1. **Lambda-based State Access:** Delegate receives state mutations via lambdas (`updateAddOn`, `onRateApplied`) rather than holding a direct `_uiState` reference. Best for async operations.
2. **Stateless / Pure:** Delegate receives all context as parameters and returns results. No internal state, no `bind()`, no `CoroutineScope`. Best for pure calculations.

---

## 5. MainScreen as Orchestrator

The `MainScreen` is the root container and orchestrator for top-level bottom navigation. Unlike leaf features, it acts as a persistent host.

### Responsibilities
1. **Bottom Navigation Management:** Holds `BottomNavigationBar` and manages switching between tabs (Groups, Expenses, Balances) via `BottomNavigationController`.
2. **Multi-Stack State Preservation:** Maintains an independent `NavHostController` per tab. `MainViewModel` saves and restores the `Bundle` state of each controller when switching tabs, preserving nested back stacks.
3. **Dynamic UI Construction:** Observes the inner route of the active tab to dynamically hoist the appropriate `ScreenUiProvider` (TopBar and FAB/MainAction) to the root Scaffold.
4. **Visual Effects & Transitions:** Initializes `SharedTransitionLayout` to enable shared element transitions and manages `HazeState` for bottom bar glassmorphism.
5. **Window Insets:** Consumes insets manually to allow scrollable content to draw behind transparent navigation bars for edge-to-edge rendering.
