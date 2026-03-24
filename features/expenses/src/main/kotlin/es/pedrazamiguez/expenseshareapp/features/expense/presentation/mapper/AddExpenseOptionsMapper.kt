package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CategoryUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentStatusUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitTypeUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Maps enum / domain option lists to their UI-model counterparts for the Add Expense form.
 *
 * Responsible for:
 * - [Currency] → [CurrencyUiModel]
 * - [PaymentMethod] / [ExpenseCategory] / [PaymentStatus] / [SplitType] → selector UI models
 * - Exchange-rate and group-amount label strings
 *
 * Extracted from [AddExpenseUiMapper] to keep class function count within the
 * configured Detekt threshold.
 */
class AddExpenseOptionsMapper(
    private val resourceProvider: ResourceProvider
) {

    fun mapCurrency(currency: Currency): CurrencyUiModel = CurrencyUiModel(
        code = currency.code,
        displayText = currency.formatDisplay(),
        decimalDigits = currency.decimalDigits
    )

    fun mapCurrencies(currencies: List<Currency>): ImmutableList<CurrencyUiModel> =
        currencies.map { mapCurrency(it) }.toImmutableList()

    fun mapPaymentMethods(methods: List<PaymentMethod>): ImmutableList<PaymentMethodUiModel> =
        methods.map { method ->
            PaymentMethodUiModel(
                id = method.name,
                displayText = resourceProvider.getString(method.toStringRes())
            )
        }.toImmutableList()

    /**
     * Maps a list of [ExpenseCategory] enums to UI models, filtering out
     * non-user-selectable categories (CONTRIBUTION, REFUND).
     */
    fun mapCategories(categories: List<ExpenseCategory>): ImmutableList<CategoryUiModel> =
        categories
            .filter { it != ExpenseCategory.CONTRIBUTION && it != ExpenseCategory.REFUND }
            .map { category ->
                CategoryUiModel(
                    id = category.name,
                    displayText = resourceProvider.getString(category.toStringRes())
                )
            }.toImmutableList()

    /**
     * Maps a list of [PaymentStatus] enums to UI models, filtering to only
     * user-selectable statuses (FINISHED, SCHEDULED).
     */
    fun mapPaymentStatuses(statuses: List<PaymentStatus>): ImmutableList<PaymentStatusUiModel> =
        statuses
            .filter { it == PaymentStatus.FINISHED || it == PaymentStatus.SCHEDULED }
            .map { status ->
                PaymentStatusUiModel(
                    id = status.name,
                    displayText = resourceProvider.getString(status.toStringRes())
                )
            }.toImmutableList()

    fun mapSplitTypes(splitTypes: List<SplitType>): ImmutableList<SplitTypeUiModel> =
        splitTypes.map { splitType ->
            SplitTypeUiModel(
                id = splitType.name,
                displayText = resourceProvider.getString(splitType.toStringRes())
            )
        }.toImmutableList()

    fun buildExchangeRateLabel(
        groupCurrency: CurrencyUiModel,
        selectedCurrency: CurrencyUiModel
    ): String = resourceProvider.getString(
        R.string.add_expense_rate_label_format,
        groupCurrency.displayText,
        selectedCurrency.displayText
    )

    fun buildGroupAmountLabel(groupCurrency: CurrencyUiModel): String =
        resourceProvider.getString(R.string.add_expense_amount_in, groupCurrency.displayText)
}
