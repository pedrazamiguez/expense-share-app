
# Cash Management & Multi-Currency ATM Withdrawals (FIFO) — Implementation Summary

## Issue #410 — Full Layer-by-Layer Implementation

### 📦 New Files Created (22 files)

**Domain Layer:**
1. `domain/.../model/CashWithdrawal.kt` — Domain model for ATM withdrawals
2. `domain/.../model/CashTranche.kt` — Tracks which withdrawal funded an expense portion
3. `domain/.../repository/CashWithdrawalRepository.kt` — Repository interface
4. `domain/.../datasource/local/LocalCashWithdrawalDataSource.kt` — Local data source interface
5. `domain/.../datasource/cloud/CloudCashWithdrawalDataSource.kt` — Cloud data source interface
6. `domain/.../service/CashWithdrawalValidationService.kt` — Validation service
7. `domain/.../usecase/balance/AddCashWithdrawalUseCase.kt` — Use case to add withdrawal
8. `domain/.../usecase/balance/GetCashWithdrawalsFlowUseCase.kt` — Use case to observe withdrawals

**Data Layer - Local (Room):**
9. `data/local/.../entity/CashWithdrawalEntity.kt` — Room entity
10. `data/local/.../dao/CashWithdrawalDao.kt` — Room DAO with merge reconciliation
11. `data/local/.../mapper/CashWithdrawalEntityMapper.kt` — Entity ↔ Domain mapper
12. `data/local/.../converter/CashTrancheListConverter.kt` — TypeConverter for JSON storage
13. `data/local/.../datasource/impl/LocalCashWithdrawalDataSourceImpl.kt` — Local data source impl

**Data Layer - Firebase (Cloud):**
14. `data/firebase/.../document/CashWithdrawalDocument.kt` — Firestore document model
15. `data/firebase/.../mapper/CashWithdrawalDocumentMapper.kt` — Document ↔ Domain mapper
16. `data/firebase/.../datasource/impl/FirestoreCashWithdrawalDataSourceImpl.kt` — Cloud data source with snapshot listeners

**Data Layer - Repository:**
17. `data/.../repository/impl/CashWithdrawalRepositoryImpl.kt` — Offline-first repository with real-time sync

**Presentation Layer:**
18. `features/balances/.../model/CashBalanceUiModel.kt` — UI model for cash balance display
19. `features/balances/.../model/CashWithdrawalUiModel.kt` — UI model for withdrawal history
20. `features/balances/.../component/WithdrawCashBottomSheet.kt` — Withdrawal form bottom sheet
21. `features/balances/.../component/CashWithdrawalHistoryItem.kt` — Activity list item for withdrawals

**Tests:**
22. `domain/.../service/CashWithdrawalValidationServiceTest.kt` — Validation service tests
23. `data/.../repository/impl/CashWithdrawalRepositoryImplTest.kt` — Repository tests with injected dispatcher

---

### ✏️ Modified Files (24 files)

**Domain Layer:**
- `Expense.kt` — Added `cashTranches: List<CashTranche>`
- `GroupPocketBalance.kt` — Renamed `balance` → `virtualBalance`, added `cashBalances: Map<String, Long>`
- `ExpenseRepository.kt` — Added `getExpenseById()`
- `ExpenseCalculatorService.kt` — Added `calculateFifoCashAmount()` FIFO logic and `hasInsufficientCash()`
- `AddExpenseUseCase.kt` — FIFO processing for CASH payment method
- `DeleteExpenseUseCase.kt` — Cash tranche refund on deletion
- `BalancesDomainModule.kt` — Registered new use cases and services
- `ExpensesDomainModule.kt` — Updated dependencies for AddExpense/DeleteExpense use cases

**Data Layer:**
- `ExpenseEntity.kt` — Added `cashTranchesJson` column
- `ExpenseEntityMapper.kt` — Handles cashTranches JSON serialization
- `ExpenseDocumentMapper.kt` — Handles cashTranches for Firestore
- `ExpenseDocument.kt` — Added `cashTranches: List<Map<String, Any>>`
- `ExpenseRepositoryImpl.kt` — Added `getExpenseById()` implementation
- `AppDatabase.kt` — Version 5→6, added CashWithdrawalEntity, CashTrancheListConverter
- `DataLocalModule.kt` — Added MIGRATION_5_6, CashWithdrawalDao, LocalCashWithdrawalDataSource
- `DataFirebaseModule.kt` — Registered CloudCashWithdrawalDataSource
- `BalancesDataModule.kt` — Registered CashWithdrawalRepository

**Presentation Layer:**
- `GroupPocketBalanceUiModel.kt` — Added `cashBalances: ImmutableList<CashBalanceUiModel>`
- `BalancesUiState.kt` — Added withdrawal dialog state fields and cashWithdrawals list
- `BalancesUiEvent.kt` — Added withdrawal events (Show/Dismiss/Update/Submit)
- `BalancesUiAction.kt` — Added ShowWithdrawalSuccess/Error actions
- `BalancesViewModel.kt` — Full withdrawal flow with validation and submission
- `BalancesUiMapper.kt` — Added `mapCashWithdrawals()` method
- `BalancesUiModule.kt` — Updated DI with new dependencies
- `BalancesScreen.kt` — Integrated WithdrawCashBottomSheet, CashWithdrawalHistoryItem, dual FABs
- `BalancesFeature.kt` — Added withdrawal action handlers
- `GroupPocketBalanceCard.kt` — Shows "Cash in hand" section

**Tests:**
- `ExpenseCalculatorServiceTest.kt` — Added 7 FIFO calculation tests
- `DeleteExpenseUseCaseTest.kt` — Added cash tranche refund tests
- `BalancesViewModelTest.kt` — Updated with new constructor dependencies

**Resources:**
- `values/strings.xml` — 13 new English strings for cash withdrawal
- `values-es/strings.xml` — 13 new Spanish strings for cash withdrawal

**Preview:**
- `PreviewData.kt` — Added PREVIEW_CASH_WITHDRAWAL_1, updated pocket balance with cashBalances

---

### 🏗️ Architecture Compliance

✅ ViewModels depend ONLY on UseCases, Mappers, and Domain Services  
✅ FIFO calculation lives in `ExpenseCalculatorService` (Domain Service)  
✅ Validation lives in `CashWithdrawalValidationService` (Domain Service)  
✅ Offline-first: Room first, then background cloud sync  
✅ Real-time multi-device sync with snapshot listeners (single-subscription pattern)  
✅ Merge reconciliation (no deleteAll + insertAll)  
✅ Injected `CoroutineDispatcher` for deterministic tests  
✅ `ImmutableList` in UiState  
✅ Feature/Screen separation  
✅ `UiText` pattern for ViewModel strings  
✅ Room migration (v5→v6) with `CashTrancheListConverter`
