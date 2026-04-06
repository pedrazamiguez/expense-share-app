package es.pedrazamiguez.splittrip.features.expense.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatDisplay
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import es.pedrazamiguez.splittrip.domain.enums.PaymentStatus
import es.pedrazamiguez.splittrip.domain.enums.SplitType
import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.extensions.toFundingSourceStringRes
import es.pedrazamiguez.splittrip.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.splittrip.features.expense.presentation.model.CategoryUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.FundingSourceUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.PaymentStatusUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.SplitTypeUiModel
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
class AddExpenseOptionsUiMapper(
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
     * Maps a list of user-selectable [PayerType] values to UI models.
     * Only GROUP and USER are user-selectable; SUBUNIT is excluded.
     */
    fun mapFundingSources(payerTypes: List<PayerType>): ImmutableList<FundingSourceUiModel> =
        payerTypes
            .filter { it != PayerType.SUBUNIT }
            .map { payerType ->
                FundingSourceUiModel(
                    id = payerType.name,
                    displayText = resourceProvider.getString(payerType.toFundingSourceStringRes())
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
