This project follows a Unidirectional Data Flow (UDF) pattern—specifically a variation of MVI (Model-View-Intent)—to ensure a clean separation of concerns, predictability, and testability.

We strictly distinguish between **State** (what the UI shows), **Events** (what the user does), and **Side Effects** (one-off occurrences).

## The Core Triad

### 1. UI State (`StateFlow`)
* **Definition:** Represents the persistent "snapshot" of the screen at any given moment.
* **Implementation:** `StateFlow<UiState>`.
* **Characteristics:**
    * **Sticky:** It always has a value. When a view subscribes, it immediately receives the latest state.
    * **Persistent:** It survives configuration changes (like screen rotations).
    * **Usage:** Use this for data that must remain visible if the user rotates the phone or navigates away and back quickly.
* **Examples:** Loading indicators, lists of data (Expenses, Groups), input field text, validation errors (e.g., "Title cannot be empty").

### 2. UI Events (`onEvent`)
* **Definition:** Represents the user's intent or inputs. These are the "Inputs" to the ViewModel.
* **Implementation:** A single public method `fun onEvent(event: UiEvent)` that accepts a sealed interface/class.
* **Characteristics:**
    * **Decoupled:** The UI does not call specific helper methods (e.g., `submitData()`). Instead, it notifies the ViewModel that an event occurred (e.g., `SubmitButtonClicked`).
    * **Traceable:** Makes debugging easier as we can log every action flowing into the ViewModel.
* **Examples:** `TitleChanged`, `SubmitPressed`, `DeleteClicked`.

### 3. UI Side Effects / Actions (`SharedFlow`)
* **Definition:** Represents ephemeral, "fire-and-forget" occurrences that should happen exactly once. These are the "One-shot Outputs".
* **Implementation:** `SharedFlow<UiAction>` (configured for no replay) or `Channel`.
* **Characteristics:**
    * **Ephemeral:** These events are not part of the state. They happen and disappear.
    * **Non-Sticky:** If the UI is not observing when the effect is emitted, it might be dropped (intentional for UI feedback).
    * **Lifecycle Aware:** They should **not** re-trigger upon screen rotation.
* **Examples:** Showing a Toast/Snackbar, navigating to another screen, vibration feedback, playing a sound.

---

## Why this distinction matters?

### State vs. Side Effects
The most common mistake is putting "One-shot" events into the `State`.
* **Bad Practice:** Adding a `showErrorToast: Boolean` to the UiState.
    * *Problem:* If the user rotates the screen, the Activity recreates, observes the State again, sees `showErrorToast = true`, and shows the Toast *again*. You then have to manually reset the boolean to false.
* **Good Practice:** Emitting a `ShowErrorToast` action via `SharedFlow`.
    * *Benefit:* The Toast is consumed once. If the screen rotates, the `SharedFlow` does not replay the old event, preventing duplicate feedback.

### Events vs. Direct Method Calls
Using `onEvent` enforces a strict API surface. The View acts purely as a dumb renderer that forwards user intents. It prevents the View from knowing *how* the ViewModel processes logic, allowing for easier refactoring of the ViewModel without breaking the UI.

---

## Code Example

### 1. The Contract (UiState, UiEvent, UiAction)
```

---

## Handler Delegation & the Delegate Sub-Pattern

### Tier 1: ViewModel → Event Handler

When a ViewModel's `onEvent()` handles **more than ~5 event categories** or the file exceeds **~200 lines**, logic is extracted into **Event Handler** classes. The ViewModel becomes a thin router.

Handlers are plain classes (NOT ViewModels) that receive `MutableStateFlow<UiState>`, `MutableSharedFlow<UiAction>`, and `CoroutineScope` via a `bind()` method. They are created inside the `viewModel { }` Koin block so cross-handler references work.

```kotlin
// Handler interface contract
interface MyFeatureEventHandler {
    fun bind(
        stateFlow: MutableStateFlow<MyUiState>,
        actionsFlow: MutableSharedFlow<MyUiAction>,
        scope: CoroutineScope
    )
}

// ViewModel as thin router
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

**Reference:** See `AddExpenseViewModel` → `ConfigEventHandler`, `CurrencyEventHandler`, `SplitEventHandler`, `SubunitSplitEventHandler`, `AddOnEventHandler`, `SubmitEventHandler` in `:features:expenses`.

### Tier 2: Event Handler → Delegate

When an Event Handler itself exceeds **~600 lines**, cohesive logic sections are extracted into **Delegate** classes. This is the same decomposition principle applied one level deeper.

| Tier | Class Type | Trigger | Role |
|------|-----------|---------|------|
| **Tier 1** | `*ViewModel` → `*EventHandler` | ViewModel >5 event categories or >200 lines | Thin router delegates events to handlers |
| **Tier 2** | `*EventHandler` → `*Delegate` | Handler >600 lines | Handler delegates cohesive logic blocks to delegates |

**Key design decisions for Delegates:**

1. **Naming:** Use `*Delegate` suffix — NOT `*EventHandler`. Delegates are plain classes that don't implement `AddExpenseEventHandler` and don't participate in `bind()`. This also avoids triggering Konsist's EventHandler isolation rules.

2. **Two valid patterns:**
   - **Lambda-based state access** — Delegate receives state access via lambdas (`updateAddOn`, `onRateApplied`) rather than holding a direct `_uiState` reference. Best when the delegate manages async operations (e.g., API calls, debouncing).
   - **Stateless/pure** — Delegate receives all context as parameters. No internal state, no `bind()`, no `CoroutineScope`. Best for synchronous calculations.

3. **DI integration** — Delegates are created inside the `viewModel { }` Koin block alongside handlers. They receive the same shared domain service instances.

```kotlin
// Lambda-based delegate (manages async operations)
class AddOnExchangeRateDelegate(
    private val exchangeRateCalculationService: ExchangeRateCalculationService,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val formattingHelper: FormattingHelper,
    // ... other domain services
) {
    fun fetchAndApplyRate(
        addOnId: String,
        sourceCurrency: Currency,
        targetCurrency: Currency,
        updateAddOn: (String, (AddOnUiModel) -> AddOnUiModel) -> Unit,
        onRateApplied: () -> Unit
    ) {
        // Async logic with debouncing, job management, etc.
    }
}

// Stateless delegate (pure calculations)
class IntraSubunitSplitDelegate(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
    private val splitPreviewService: SplitPreviewService,
    private val formattingHelper: FormattingHelper
) {
    fun recalculateIntraSubunitSplits(
        entitySplits: List<EntitySplitUiModel>,
        groupSubunits: List<Subunit>,
        splitType: SplitType,
        decimalDigits: Int
    ): List<EntitySplitUiModel> {
        // Pure calculation, all context passed as parameters
    }
}
```

**Reference:** See `AddOnExchangeRateDelegate` and `IntraSubunitSplitDelegate` in `:features:expenses`.

### Enforcement

A **Konsist architecture test** enforces a **600-line hard limit** on all production source files. Test files are exempt. The CI check fails if any production `.kt` file exceeds the threshold.kotlin
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
    object SubmitPressed : AddExpenseUiEvent
}

// Action: One-shot Side Effects
sealed interface AddExpenseUiAction {
    data class ShowSnackbar(val message: String) : AddExpenseUiAction
    object NavigateBack : AddExpenseUiAction
}
