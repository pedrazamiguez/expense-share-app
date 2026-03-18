package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event

sealed interface AddExpenseUiEvent {
    data class LoadGroupConfig(val groupId: String?) : AddExpenseUiEvent
    data class RetryLoadConfig(val groupId: String?) : AddExpenseUiEvent
    data class TitleChanged(val title: String) : AddExpenseUiEvent
    data class SourceAmountChanged(val amount: String) : AddExpenseUiEvent
    data class CurrencySelected(val currencyCode: String) : AddExpenseUiEvent
    data class PaymentMethodSelected(val methodId: String) : AddExpenseUiEvent
    data class ExchangeRateChanged(val rate: String) : AddExpenseUiEvent
    data class GroupAmountChanged(val amount: String) : AddExpenseUiEvent
    data class CategorySelected(val categoryId: String) : AddExpenseUiEvent
    data class VendorChanged(val vendor: String) : AddExpenseUiEvent
    data class NotesChanged(val notes: String) : AddExpenseUiEvent
    data class PaymentStatusSelected(val statusId: String) : AddExpenseUiEvent
    data class DueDateSelected(val dateMillis: Long) : AddExpenseUiEvent
    data class ReceiptImageSelected(val uri: String) : AddExpenseUiEvent
    data object RemoveReceiptImage : AddExpenseUiEvent
    data class SubmitAddExpense(val groupId: String?) : AddExpenseUiEvent

    // Split events
    data class SplitTypeChanged(val splitTypeId: String) : AddExpenseUiEvent
    data class SplitAmountChanged(val userId: String, val amount: String) : AddExpenseUiEvent
    data class SplitPercentageChanged(val userId: String, val percentage: String) : AddExpenseUiEvent
    data class SplitExcludedToggled(val userId: String) : AddExpenseUiEvent

    // Sub-unit split events
    /** Toggles between flat member splitting and entity-level (sub-unit) splitting. */
    data object SubunitModeToggled : AddExpenseUiEvent
    /** Level 1 — Entity excluded toggle (solo user or entire sub-unit). */
    data class EntitySplitExcludedToggled(val entityId: String) : AddExpenseUiEvent
    /** Level 1 — Entity amount changed (EXACT mode at entity level). */
    data class EntitySplitAmountChanged(val entityId: String, val amount: String) : AddExpenseUiEvent
    /** Level 1 — Entity percentage changed (PERCENT mode at entity level). */
    data class EntitySplitPercentageChanged(val entityId: String, val percentage: String) : AddExpenseUiEvent
    /** Level 2 — Intra-sub-unit split type changed (per sub-unit strategy). */
    data class IntraSubunitSplitTypeChanged(val subunitId: String, val splitTypeId: String) : AddExpenseUiEvent
    /** Level 2 — Intra-sub-unit member amount changed (EXACT within sub-unit). */
    data class IntraSubunitAmountChanged(val subunitId: String, val userId: String, val amount: String) : AddExpenseUiEvent
    /** Level 2 — Intra-sub-unit member percentage changed (PERCENT within sub-unit). */
    data class IntraSubunitPercentageChanged(val subunitId: String, val userId: String, val percentage: String) : AddExpenseUiEvent
    /** Toggles sub-unit accordion expansion. */
    data class EntityAccordionToggled(val entityId: String) : AddExpenseUiEvent
}
