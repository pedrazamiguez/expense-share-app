Moving validation logic out of the ViewModel is essential for a clean, testable, and robust architecture. This entry explains why we avoid ad-hoc validation and how we distinguish between **Use Cases** and **Domain Services**.

## The Anti-Pattern: Ad-Hoc Validation

It is common to see ViewModels cluttered with `if/else` checks to validate user input:

```kotlin
// ❌ BAD: ViewModel holding business rules
fun onTitleChanged(newTitle: String) {
    if (newTitle.isBlank()) {
        _state.update { it.copy(error = "Title cannot be empty") }
    } else if (newTitle.length > 50) {
        _state.update { it.copy(error = "Title too long") }
    }
}

```

### Why is this bad?

1. **Violation of SRP:** The ViewModel's job is to manage UI State, not to define what makes an expense "valid."
2. **Hard to Test:** Testing these rules requires instantiating the ViewModel and mocking UI flows.
3. **Low Reusability:** If another screen needs to edit an expense, you have to duplicate these `if` checks.

---

## The Solution: Extraction to Domain Layer

We move this logic to the **Domain Layer**. However, we must choose the correct component type.

### Naming Matters: Use Case vs. Domain Service

A common question is: *"Is validation a Use Case?"*
The answer is usually **no**.

* **Use Case (`AddExpenseUseCase`):** Represents a specific **user intention** or action. It orchestrates a flow (e.g., fetch data, save to DB, notify user). It implies a transaction.
* **Domain Service (`ExpenseValidationService`):** Represents **intrinsic business logic** or rules. It is a support component. Validating data is not a "goal" in itself; it is a requirement to achieve a goal.

Therefore, we use a **Service** (or a Validator) for this logic.

* **Correct:** `ExpenseValidationService` / `ExpenseValidator`
* **Incorrect:** `ValidateExpenseTitleUseCase` (Validation is a check, not a user journey).

## Implementation Strategy

### 1. The Domain Service

Located in the Domain module. It is a pure Kotlin class with no dependencies on Android or Repositories.

```kotlin
class ExpenseValidationService {

    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Invalid("Title cannot be empty")
            title.length > 20 -> ValidationResult.Invalid("Title is too long")
            else -> ValidationResult.Valid
        }
    }

    fun validateAmount(amount: String): ValidationResult {
        // Complex regex or parsing logic here...
    }
}

```

### 2. The ViewModel Integration

The ViewModel injects this service. It calls the service on every input change to provide **real-time feedback** (excellent UX) without knowing the actual rules.

```kotlin
class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,       // The Action
    private val validationService: ExpenseValidationService // The Rules
) : ViewModel() {

    fun onEvent(event: AddExpenseUiEvent) {
        when(event) {
            is AddExpenseUiEvent.TitleChanged -> {
                val result = validationService.validateTitle(event.title)
                
                _uiState.update { 
                    it.copy(
                        title = event.title,
                        titleError = if (result is ValidationResult.Invalid) result.message else null
                    )
                }
            }
            
            is AddExpenseUiEvent.Submit -> {
                // Final check before calling the UseCase
                if (_uiState.value.isValid) {
                     addExpenseUseCase(params)
                }
            }
        }
    }
}
```

## Benefits of this Approach

1. **Clean ViewModels:** The ViewModel only acts as a traffic controller between the View and the Domain.
2. **Centralized Rules:** If the rule "Title max length" changes from 20 to 50, you update it in *one* place (`ExpenseValidationService`), and it updates everywhere (Add Screen, Edit Screen, Import Screen).
3. **Pure Unit Tests:** You can test `ExpenseValidationService` with simple input/output tests, covering all edge cases without needing a ViewModel or UI.
