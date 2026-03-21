package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CategoryUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentStatusUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitTypeUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val isLoadingRate: Boolean = false,
    val isConfigLoaded: Boolean = false,
    val configLoadFailed: Boolean = false,
    val loadedGroupId: String? = null,
    val groupName: String? = null,

    // Inputs
    val expenseTitle: String = "",
    val sourceAmount: String = "",
    val vendor: String = "",
    val notes: String = "",

    // Selection
    val selectedCurrency: CurrencyUiModel? = null,
    val selectedPaymentMethod: PaymentMethodUiModel? = null,
    val selectedCategory: CategoryUiModel? = null,
    val selectedPaymentStatus: PaymentStatusUiModel? = null,

    // Calculated / Display Data
    val groupCurrency: CurrencyUiModel? = null,
    /**
     * User-friendly exchange rate displayed in the UI.
     * Represents "1 [GroupCurrency] = X [SourceCurrency]" (e.g., "1 EUR = 37 THB").
     * This is the INVERSE of the internal calculation rate.
     */
    val displayExchangeRate: String = "1.0",
    val calculatedGroupAmount: String = "", // "Cost in EUR"
    val showExchangeRateSection: Boolean = false,
    /**
     * True when the exchange rate is determined by ATM withdrawal rates (CASH payment)
     * and should not be editable by the user.
     */
    val isExchangeRateLocked: Boolean = false,
    /**
     * Informational message explaining why the rate is locked.
     * Shown in the exchange rate section when [isExchangeRateLocked] is true.
     */
    val exchangeRateLockedHint: UiText? = null,
    /**
     * Snapshot of [displayExchangeRate] taken just before switching to CASH payment.
     * Restored when the user switches back to a non-CASH method so the custom
     * (or previously fetched) rate is not lost.
     * Cleared when the selected currency changes because the saved rate would
     * belong to a different currency pair.
     */
    val preCashExchangeRate: String? = null,
    /**
     * True when the entered source amount exceeds the available cash withdrawals.
     * Drives warning styling in the exchange rate hint.
     */
    val isInsufficientCash: Boolean = false,
    val showDueDateSection: Boolean = false,

    // Due date
    val dueDateMillis: Long? = null,
    val formattedDueDate: String = "",

    // Receipt image
    val receiptUri: String? = null,

    // Pre-formatted labels for the exchange rate section
    val exchangeRateLabel: String = "",
    val groupAmountLabel: String = "",

    // Data Lists
    val availableCurrencies: ImmutableList<CurrencyUiModel> = persistentListOf(),
    val paymentMethods: ImmutableList<PaymentMethodUiModel> = persistentListOf(),
    val availableCategories: ImmutableList<CategoryUiModel> = persistentListOf(),
    val availablePaymentStatuses: ImmutableList<PaymentStatusUiModel> = persistentListOf(),

    // Split Configuration
    val availableSplitTypes: ImmutableList<SplitTypeUiModel> = persistentListOf(),
    val selectedSplitType: SplitTypeUiModel? = null,
    val splits: ImmutableList<SplitUiModel> = persistentListOf(),
    val splitError: UiText? = null,
    val memberIds: ImmutableList<String> = persistentListOf(),

    // Add-On Configuration
    val addOns: ImmutableList<AddOnUiModel> = persistentListOf(),
    val isAddOnsSectionExpanded: Boolean = false,
    val addOnError: UiText? = null,
    /** Formatted effective total (base + ON_TOP add-ons − discounts) for display. */
    val effectiveTotal: String = "",

    // Sub-unit split mode
    /** True when the group has sub-units available (controls toggle visibility). */
    val hasSubunits: Boolean = false,
    /** True when "Split by sub-unit" toggle is active. */
    val isSubunitMode: Boolean = false,
    /** Entity-level splits (solo users + sub-unit headers) for sub-unit mode. */
    val entitySplits: ImmutableList<SplitUiModel> = persistentListOf(),

    // Errors
    val error: UiText? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true,
    val isDueDateValid: Boolean = true
) {
    /**
     * Returns true when the screen is ready for user interaction.
     * The form should only be shown when config is loaded and not failed.
     */
    val isReady: Boolean
        get() = isConfigLoaded && !configLoadFailed && !isLoading

    /**
     * Returns true when the form inputs are valid and ready for submission.
     */
    val isFormValid: Boolean
        get() = isTitleValid &&
            isAmountValid &&
            isDueDateValid &&
            addOns.all { it.isAmountValid } &&
            expenseTitle.isNotBlank() &&
            sourceAmount.isNotBlank()
}
