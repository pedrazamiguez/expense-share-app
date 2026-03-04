title:	Cash Management and Multi-Currency ATM Withdrawals
state:	OPEN
author:	pedrazamiguez
labels:	feature
comments:	1
assignees:	pedrazamiguez
projects:	
milestone:	
number:	410
--
### Problem Statement

I need your help with developing a plan to implement a new feature for cash withdrawals in group balance and expenses on my android app.

Balances and expenses are only visible as long as a group is selected.

On balances screen, each group member can deposit (add money) to the group pocket. Group balance shows sum(group deposits) minus sum(group expenses).

On balances screen at the moment, there is also a list of activity that shows each deposit made by each member.

On expenses screen, each member can add an expense (on multiple currencies, but not relevant for this feature I believe). On eventual features, such member adding an expense will be able to determine how it is split among members (for now evenly) and if the expense was paid by themeselves or by the group pocket allowance (at the moment, all expenses are paid with the group balance).

Well, after this context, what I'd like to implement is paying with cash management. 

Let's say Antonio and I go on a trip to Thailand. Each of us add up 2000 EUR to the group pocket. Pocket is 4000 EUR.

Now, we pay plane tickets 1500 EUR (expense), so group balance is 2500 EUR. Good so far.

Now, we arrive in Thailand, and we need cash. We go to an ATM and withdraw 10000 THB (270 EUR). Let's forget about ATM fees or taxes for now to simplify this, but I guess that would be automatically logged as an expense, this will be optional.

In this scenario, withdrawing cash is not considered an expense. I would need to be handling two balances per se. One virtual / card, another one in CASH. Also, I would need the exact exchange rate applied by the ATM to know exactly the conversion. Let's see an example. 

Initial group balance 4000 EUR

Add expense of 1500 EUR for plane tickets

Group balance 2500 EUR

Withdraw 10000 THB from ATM (equivalent to 270 EUR, exchange rate: 37.037)
Optional: 8 EUR fee

Group balance 2222 EUR
Group balance cash 10000 THB / 270 EUR (cash balance displayed on both currencies, if different from the group's default currency - same logic as expense amount)

Optional: 8 EUR fee automatically logged as an expense

Now, the interesting part of this (or what I'd like to accomplish) is: when I log an expense and select CASH as payment method, this expense will be taken into account for the CASH group balance, not the virtual group balance, and automatically will use the exchange rate applied in the ATM at the moment of the withdrawal (that's why I need to save the rate applied).

I guess withdrawing cash on an ATM will be a feature accessible from Balances screen, and will be a form to fill all information needed. In what currency (if same currency as the group default, exchange rate will be 1.0 and won't be asked) and optionally fees o taxes (in group's default currency).

---

As an extended feature, bare in mind that it's quite normal to withdraw cash a few times during a trip, and numbers can be completely different, in terms of currency (e.g. I can withdraw in Spain in EUR, in Thailand in THB or in any other currency in any other country), even if on the same currency, exchange rates may vary, and also fees depending on the bank).

the idea is if I still have some little cash, and I need to withdraw again, to log a second (third and as many times as needed) withdrawal, this needs to be taking into account when calculating cash balance and adding expenses using CASH payment method.

If I have left 50 THB from my first time withdrawing at rate 37.82, and more Bahts from a second withdrawal at rate 36.8, then I pay in CASH a souvenir that costs 230 THB, this needs to be calculated properly (50 THB at 37.82, and the rest 180 THB at 36.8, then converted into EUR).

Let me know if this makes sense or you need more details.

I need you to create a detailed step by step plan to implement this properly on my app since I need to write a github issue describing this feature to implement it.

### Proposed Solution


## Objective

Introduce the ability to manage a physical "Cash" balance alongside the existing "Virtual" group balance. This includes logging ATM withdrawals with specific exchange rates, optionally logging ATM fees as expenses, and applying a FIFO (First-In, First-Out) calculation when paying for expenses in cash to ensure accurate conversions to the group's default currency.

## Phase 1: Data Modelling & Storage (Domain & Data Layers)

**1. Update `PaymentMethod` Enum**

* Ensure `PaymentMethod.kt` clearly distinguishes between `VIRTUAL_POCKET` (or `CARD`) and `CASH`.

**2. Create `CashWithdrawal` Entity & Document**

* Create a new model to track individual ATM withdrawals.
* **Fields required:**
* `id`: String
* `groupId`: String
* `withdrawnBy`: String (userId)
* `amountWithdrawn`: BigDecimal (e.g., 10000 THB)
* `currency`: String (e.g., THB)
* `deductedBaseAmount`: BigDecimal (e.g., 270 EUR)
* `exchangeRate`: Double (Calculated or explicitly provided)
* `timestamp`: Long
* `remainingAmount`: BigDecimal (Crucial for FIFO: starts at 10000 THB, decreases as cash expenses are logged).



**3. Update `Expense` Model**

* When an expense is paid in `CASH`, we need to know exactly which withdrawal(s) funded it so we can revert it if the expense is deleted.
* Add an optional field to `Expense.kt`: `cashTranchesConsumed: List<CashTranche>`.
* A `CashTranche` would hold `withdrawalId` and `amountConsumed`.

**4. Update `GroupPocketBalance**`

* Modify the balance model to separate the `virtualBalance` (default currency) from `cashBalances` (a Map or List of currencies and their current total remaining amounts).

## Phase 2: Business Logic & FIFO (Domain Layer)

**1. Create `AddCashWithdrawalUseCase**`

* This use case will handle the submission of the withdrawal form.
* **Actions:**
* Create a negative contribution or deduction transaction against the virtual pocket for the `deductedBaseAmount`.
* Create the new `CashWithdrawal` record.
* *Optional Fee Logic:* If an ATM fee is provided (e.g., 8 EUR), automatically call `AddExpenseUseCase` to log this fee, paid by the virtual pocket.



**2. Implement FIFO Logic in `ExpenseCalculatorService**`

* When `AddExpenseUseCase` is called with `PaymentMethod.CASH`:
* Fetch all `CashWithdrawal` records for the group in the matching currency where `remainingAmount > 0`, ordered by `timestamp` ascending.
* Iterate through these records to fulfil the expense amount.
* *Example:* Expense is 230 THB. Withdrawal 1 has 50 THB remaining (Rate 37.82). Withdrawal 2 has 5000 THB remaining (Rate 36.80).
* Deduct 50 THB from Withdrawal 1 (remaining becomes 0). Deduct 180 THB from Withdrawal 2 (remaining becomes 4820).
* Calculate the exact base currency cost (EUR) using those specific rates to update the group's total spending accurately.
* Save the `CashTranche` data within the `Expense`.



**3. Update `DeleteExpenseUseCase**`

* If a user deletes a cash expense, iterate through its `cashTranchesConsumed` and restore the `remainingAmount` to the respective `CashWithdrawal` records.

## Phase 3: UI Updates – Balances Screen

**1. Update `GroupPocketBalanceCard**`

* Redesign the card to show the "Virtual Pocket" balance alongside a breakdown of "Cash Pockets".
* Display cash balances in their native currency and their equivalent base currency value (e.g., "Cash: 10,000 THB / ~270 EUR").

**2. Add "Withdraw Cash" Action**

* Introduce a new button (perhaps expanding the main FAB) for "Withdraw Cash".

**3. Create `WithdrawCashBottomSheet` (or Dialog)**

* **Form fields:**
* Target Currency (Dropdown).
* Amount Withdrawn (Target Currency).
* Equivalent Base Currency Deducted (Group Default Currency) *or* an Exchange Rate input field.
* Optional: ATM Fee / Taxes (in Base Currency) with a checkbox to "Log fee as a group expense".



**4. Update Activity History**

* Ensure the activity feed displays "ATM Withdrawals" as a distinct item, separate from standard deposits or expenses.

## Phase 4: UI Updates – Expenses Screen

**1. Update `AddExpenseForm**`

* Ensure the `PaymentMethodChips` component clearly presents the `CASH` option.
* **Validation:** If the user selects `CASH` and inputs an amount greater than the current total `cashBalances` for that currency, show a warning. Do not allow them to spend cash they haven't withdrawn in the app.

## Edge Cases to Consider

* **Emptying the Cash Pocket:** If the group leaves a country and wants to return the leftover cash to the virtual pocket (e.g., depositing leftover Bahts at an exchange desk at the airport), you will eventually need a "Deposit Cash" or "Convert Cash" action. For this initial feature, you might leave this out and address it in a future iteration.
* **Deleting a Withdrawal:** If a user deletes an ATM withdrawal record that has *already* been partially consumed by expenses, you will need a strategy (either block the deletion until the expenses are deleted, or automatically convert those expenses to `VIRTUAL` payment method). Blocking deletion is usually the safest approach.

### Alternative Solutions

Here is a highly detailed, layer-by-layer specification formatted specifically for a GitHub Issue. It explicitly lists the components, files to create or modify, and enforces your Clean Architecture, offline-first, and testing standards.

---

# Feature: Cash Management & Multi-Currency ATM Withdrawals (FIFO)

## 📖 User Story

As a user travelling with a group, I need to track physical cash withdrawn from ATMs (e.g., Bahts in Thailand) separately from the group's virtual balance. When logging an expense paid in cash, the app must automatically apply the exact historical exchange rate from my previous ATM withdrawals using a First-In, First-Out (FIFO) method, ensuring perfectly accurate multi-currency conversions.

## ✅ Acceptance Criteria

1. **Withdraw Cash Action:** Users can log an ATM withdrawal specifying the target currency, amount withdrawn, equivalent deducted in base currency, and an optional ATM fee.
2. **Distinct Balances:** The Balances screen displays the "Virtual" balance separately from a breakdown of "Cash" balances grouped by currency.
3. **FIFO Expense Calculation:** When an expense is paid in `CASH`, the system automatically deducts the amount from the oldest available withdrawal(s) for that currency and applies those specific exchange rates to calculate the group cost.
4. **Validation:** Users cannot log a cash expense if the required physical cash balance is insufficient.
5. **Restoration on Deletion:** Deleting a cash expense fully restores the consumed physical cash back to its original withdrawal records.

---

## 🛠️ Technical Implementation Plan

### 1. Domain Layer (Pure Business Logic)

*No database or UI dependencies.*

**New Models (`domain/src/main/kotlin/.../model/`)**

* **Create `CashWithdrawal.kt**`:
`id`, `groupId`, `withdrawnBy`, `amountWithdrawn` (Long, cents), `remainingAmount` (Long, cents), `currency` (String), `deductedBaseAmount` (Long, cents), `exchangeRate` (Double), `createdAt`.
* **Create `CashTranche.kt**`:
`withdrawalId` (String), `amountConsumed` (Long). *Tracks exactly which withdrawal funded an expense.*

**Modified Models**

* **Update `Expense.kt**`: Add `val cashTranches: List<CashTranche> = emptyList()`.
* **Update `GroupPocketBalance.kt**`: Add `val cashBalances: Map<String, Long> = emptyMap()`. Rename the existing `balance` property to `virtualBalance`.

**Domain Services (`domain/src/main/kotlin/.../service/`)**

* **Modify `ExpenseCalculatorService.kt**`:
Add `fun calculateFifoCashAmount(amountToCover: Long, availableWithdrawals: List<CashWithdrawal>): Pair<Long, List<CashTranche>>`.
*Logic:* Iterate over withdrawals sorted by `createdAt`. Deduct from `remainingAmount` until `amountToCover` is 0. Calculate the blended base currency amount. Return the total base amount and the list of generated `CashTranche`s.

**Use Cases (`domain/src/main/kotlin/.../usecase/`)**

* **Create `AddCashWithdrawalUseCase.kt**`: Validates input, saves the withdrawal via `CashWithdrawalRepository`, and triggers `AddExpenseUseCase` if an ATM fee is present.
* **Modify `AddExpenseUseCase.kt**`: If `paymentMethod == CASH`, fetch available withdrawals via repository, execute `calculateFifoCashAmount`, update the withdrawals' remaining balances in the repository, attach the tranches to the `Expense`, and save.
* **Modify `DeleteExpenseUseCase.kt**`: If the expense has `cashTranches`, refund the `amountConsumed` back to the respective `CashWithdrawal` records.

---

### 2. Data Layer (Offline-First Architecture)

*Strict adherence to local-first writes and background cloud syncing.*

**Local Database (Room) (`data/local/src/main/kotlin/.../`)**

* **Create `CashWithdrawalEntity.kt` & `CashWithdrawalDao.kt**`: Standard CRUD.
* **Modify `ExpenseEntity.kt**`: Add `cashTranchesJson: String?`. Room cannot store lists natively.
* **Create `CashTrancheListConverter.kt**`: Room `TypeConverter` to serialize/deserialize `List<CashTranche>` to JSON.
* **Update `AppDatabase.kt**`: Increment schema version and register the new Dao and Converter.

**Remote Database (Firestore) (`data/firebase/src/main/kotlin/.../`)**

* **Create `CashWithdrawalDocument.kt**`: Firestore representation.
* **Modify `ExpenseDocument.kt**`: Add `cashTranches` (Firestore handles lists of maps natively).

**Repositories (`data/src/main/kotlin/.../repository/impl/`)**

* **Create `CashWithdrawalRepositoryImpl.kt**`:
Must implement the offline-first sync pattern.
```kotlin
class CashWithdrawalRepositoryImpl(
    private val localDataSource: LocalCashWithdrawalDataSource,
    private val cloudDataSource: CloudCashWithdrawalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO // Must be injected
) : CashWithdrawalRepository {
    override suspend fun addWithdrawal(withdrawal: CashWithdrawal) {
        localDataSource.addWithdrawal(withdrawal) // Write local instantly
        CoroutineScope(ioDispatcher).launch { // Fire and forget sync
            cloudDataSource.addWithdrawal(withdrawal)
        }
    }
}

```



---

### 3. Presentation Layer (UI & ViewModels)

*Strict adherence to ViewModel dependency rules (UseCases only, no Repositories).*

**ViewModels**

* **Create `AddCashWithdrawalViewModel.kt**`: Manages state for the withdrawal form (`amount`, `currency`, `deductedBaseAmount`, `fee`). Injects `AddCashWithdrawalUseCase`.
* **Modify `AddExpenseViewModel.kt**`:
* Inject `GetGroupPocketBalanceFlowUseCase`.
* In `submitExpense()`: Add validation. If `CASH` is selected, check if `sourceAmount` > `currentPocketBalance.cashBalances[sourceCurrency]`. If true, emit an error state (`"Insufficient cash recorded"`) and halt submission.



**UI Components / Screens**

* **Create `WithdrawCashBottomSheet.kt**`:
A form containing: Target Currency (SearchableChipSelector), Withdrawn Amount (StyledOutlinedTextField), Deducted Base Amount, Optional ATM Fee toggle.
* **Modify `GroupPocketBalanceCard.kt**`:
Refactor layout. Top section shows `Virtual Balance`. Bottom section iterates over `cashBalances` map, displaying a flag/currency icon and the remaining cash amount for each.
* **Modify `AddExpenseForm.kt**`: Ensure the `PaymentMethodChips` clearly allow selecting `CASH` and visually respond to insufficient balance errors.

---

### 4. Testing Requirements

*Must strictly follow the project's testing manifesto.*

1. **`CashWithdrawalRepositoryImplTest`**:
* **Rule:** Must inject `StandardTestDispatcher()`.
* **Test:** Call `addWithdrawal()`. You **MUST** call `advanceUntilIdle()` before using `coVerify` to ensure the background coroutine syncing to Firestore has completed deterministically.


2. **`ExpenseCalculatorServiceTest`**:
* **Test `calculateFifoCashAmount**`: Provide a mock list of two withdrawals with different rates (e.g., 50 THB @ 37.82, 5000 THB @ 36.80). Request to cover 230 THB. Assert it correctly exhausts the 50 THB tranche, takes 180 THB from the second, and returns the mathematically exact base currency total.


3. **`DeleteExpenseUseCaseTest`**:
* **Test:** Delete a mock cash expense containing `CashTranche`s. Verify `cashWithdrawalRepository` was called to restore the exact amounts to the correct withdrawal IDs.

### Priority

Critical - Essential functionality

### Feature Scope

- [x] User Interface (UI)
- [x] User Experience (UX)
- [x] Backend/Data
- [ ] Performance
- [ ] Security
- [ ] Integration with external services

### Mockups/Wireframes

_No response_

### Additional Context


### 1. The Core Concept: Why FIFO and Tranches?

When you and Antonio withdraw 10,000 THB at an exchange rate of 37.037, and later withdraw another 5,000 THB at 36.800, your physical cash pool contains mixed values. If you spend 12,000 THB, the application must deduct the first 10,000 THB at the first rate, and the remaining 2,000 THB at the second rate.

To achieve this accurately, we cannot simply store a single "Cash Balance" number. We must track every individual ATM withdrawal. When an expense is paid in cash, the system must link that expense to the specific withdrawal(s) it consumed. We call these links **Cash Tranches**.

**Why do we need Cash Tranches saved on the Expense?**
If you later delete or edit that 12,000 THB souvenir expense, the system needs to know exactly which withdrawals to refund. Without saving the tranches inside the expense record, reverting the exact amounts to the correct historical exchange rates is impossible.

---

### 2. Domain Layer: Models, Services, and Use Cases

This layer contains the pure business logic. It does not know about databases or UI.

#### A. New and Modified Models (`domain/src/main/kotlin/.../model/`)

**1. Create `CashWithdrawal.kt**`

* **What it is:** Represents a single ATM transaction.
* **Fields:** `id`, `groupId`, `amountWithdrawn` (e.g., 10000 THB), `remainingAmount` (starts at 10000, decreases as expenses consume it), `currency` ("THB"), `exchangeRate` (37.037), `createdAt`.
* **Why:** To maintain a ledger of available physical cash and lock in historical exchange rates.

**2. Create `CashTranche.kt**`

* **What it is:** A simple data class linking an expense to a withdrawal.
* **Fields:** `withdrawalId` (String), `amountConsumed` (Long).
* **Why:** As explained above, it acts as a receipt of exactly which ATM withdrawals funded a specific expense, crucial for deletions or edits.

**3. Modify `Expense.kt**`

* **Change:** Add `val cashTranches: List<CashTranche> = emptyList()`
* **Why:** To store the tranches permanently alongside the expense data.

**4. Modify `GroupPocketBalance.kt**`

* **Change:** Add `val cashBalances: Map<String, Long> = emptyMap()`. Rename `balance` to `virtualBalance`.
* **Why:** The balance screen needs to display standard virtual money (e.g., total EUR in the app) completely separate from physical cash in hand, grouped by currency code (e.g., `"THB" -> 5000L`).

#### B. Domain Services (`domain/src/main/kotlin/.../service/`)

**1. Modify `ExpenseCalculatorService.kt**`

* **Change:** Add a new function `calculateFifoCashAmount(amountToCover: Long, availableWithdrawals: List<CashWithdrawal>): Pair<Long, List<CashTranche>>`
* **Why:** Business logic for calculating money belongs in a dedicated service, not in a UseCase. This function will iterate through `availableWithdrawals` (ordered by oldest first), subtract the required amounts until `amountToCover` hits zero, generate the `CashTranche` list, and return the exact blended cost in the group's default currency.

#### C. Use Cases (`domain/src/main/kotlin/.../usecase/`)

**1. Create `AddCashWithdrawalUseCase.kt**`

* **What it does:** Validates the input, calls `cashWithdrawalRepository.addWithdrawal()`, and optionally creates a standard `Expense` if the user declared an ATM fee.
* **Why:** Encapsulates the logic of logging a new ATM visit.

**2. Modify `AddExpenseUseCase.kt**`

* **Change:** Before saving the expense, check if `expense.paymentMethod == PaymentMethod.CASH`. If yes:
1. Fetch available `CashWithdrawal`s for that currency.
2. Call `ExpenseCalculatorService.calculateFifoCashAmount`.
3. Update the `remainingAmount` on those specific `CashWithdrawal` records via the repository.
4. Attach the returned tranches and blended group amount to the `Expense` before saving it.


* **Why:** This enforces the FIFO rule at the moment an expense is created.

**3. Modify `DeleteExpenseUseCase.kt**`

* **Change:** If the deleted expense has `cashTranches`, iterate over them and add the `amountConsumed` back to the `remainingAmount` of the corresponding `CashWithdrawal` records.
* **Why:** Ensures physical cash balances are restored if a cash payment is cancelled.

---

### 3. Data Layer: Offline-First & Repositories

Following your `copilot-instructions.md`, this layer must write to the local Room database first, then sync to Firestore in the background using injected coroutine dispatchers.

**1. Room Database (`data/local/.../entity/`)**

* **Create `CashWithdrawalEntity.kt` & `CashWithdrawalDao.kt**` for standard CRUD operations.
* **Modify `ExpenseEntity.kt`:** Room cannot store a `List<CashTranche>` natively. Add a field `val cashTranchesJson: String?`.
* **Create a TypeConverter:** Create a converter to serialize `List<CashTranche>` to a JSON string when saving to Room, and deserialize it back when reading.
* **Why:** SQLite requires primitive types; TypeConverters bridge the gap for complex domain objects.

**2. Firebase Firestore (`data/firebase/.../document/`)**

* **Create `CashWithdrawalDocument.kt`:** Mirrors the domain model for cloud storage.
* **Modify `ExpenseDocument.kt`:** Add `cashTranches` (Firestore can natively handle lists of maps/objects, so JSON conversion isn't strictly necessary here, but keeping it aligned with a data mapper is best).

**3. Repository Implementation (`data/src/main/kotlin/.../repository/impl/`)**

* **Create `CashWithdrawalRepositoryImpl.kt`:** ```kotlin
class CashWithdrawalRepositoryImpl(
private val localDataSource: LocalCashWithdrawalDataSource,
private val cloudDataSource: CloudCashWithdrawalDataSource,
private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CashWithdrawalRepository {
override suspend fun addWithdrawal(withdrawal: CashWithdrawal) {
localDataSource.addWithdrawal(withdrawal) // Offline-first write
CoroutineScope(ioDispatcher).launch { // Fire and forget sync
cloudDataSource.addWithdrawal(withdrawal)
}
}
}
```

```


* **Why:** This strictly adheres to the offline-first strategy and the injected dispatcher rule outlined in your technical manifesto.

---

### 4. Presentation Layer: ViewModels and UI

**1. Modify `AddExpenseViewModel.kt**`

* **Change:** In the `submitExpense` function, if the selected payment method is `CASH`, validate that the `sourceAmount` does not exceed the total available cash for that currency found in `GroupPocketBalance`.
* **Why:** Prevents the system from attempting a FIFO calculation on cash that doesn't exist, failing fast at the UI level and showing an error message like "Insufficient cash recorded."

**2. Create `AddCashWithdrawalViewModel.kt**`

* **What it does:** Manages the state for the new "Withdraw Cash" form. It injects `AddCashWithdrawalUseCase`.
* **Why:** Follows the standard pattern for isolating screen state and events.

**3. UI Components**

* **`WithdrawCashBottomSheet` (New):** A form asking for Target Currency, Amount Withdrawn, Equivalent Deducted in Base Currency, and an optional ATM fee toggle.
* **`GroupPocketBalanceCard` (Modify):** Update the visual layout to show the `virtualBalance` distinct from a list of `cashBalances` (e.g., showing a THB flag and the remaining amount).

---

### 5. Testing Requirements

Based strictly on the rules in `copilot-instructions.md`, here are the necessary tests:

**1. `CashWithdrawalRepositoryImplTest**`

* **Must implement:** Use `StandardTestDispatcher()`. Inject this dispatcher into the repository.
* **The test:** Call `repository.addWithdrawal()`, then absolutely mandate the use of `advanceUntilIdle()` before using `coVerify` to ensure the background cloud sync coroutine has actually executed.
* **Why:** If you do not use `advanceUntilIdle()` with the injected dispatcher, the test will pass or fail unpredictably due to race conditions.

**2. `ExpenseCalculatorServiceTest**`

* **Must implement:** Unit tests for `calculateFifoCashAmount`.
* **The test:** Provide a scenario matching your prompt: Create a list of two historical withdrawals (e.g., 50 THB at rate A, 5000 THB at rate B). Request an amount of 230 THB. Assert that the resulting tranches correctly pulled 50 from the first and 180 from the second, and that the blended base currency cost is mathematically correct.
* **Why:** This is the most complex mathematical operation in the feature. It must be isolated and verified without any Android or database dependencies.

**3. `DeleteExpenseUseCaseTest**`

* **Must implement:** Create an expense with mock `CashTranche` data. Call the use case. Verify that `cashWithdrawalRepository.updateRemainingAmount()` was called with the correct refunded values.
* **Why:** To ensure deleting an expense doesn't permanently lose the physical cash balance it consumed.
