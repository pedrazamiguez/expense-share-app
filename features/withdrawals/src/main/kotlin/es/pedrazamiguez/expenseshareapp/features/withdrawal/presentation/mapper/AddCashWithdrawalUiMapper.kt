package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.withdrawal.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class AddCashWithdrawalUiMapper(
    private val resourceProvider: ResourceProvider
) {

    // ── Domain → UI Model Mapping ──────────────────────────────────────────

    fun mapCurrency(currency: Currency): CurrencyUiModel = CurrencyUiModel(
        code = currency.code,
        displayText = currency.formatDisplay(),
        decimalDigits = currency.decimalDigits
    )

    fun mapCurrencies(currencies: List<Currency>): ImmutableList<CurrencyUiModel> = currencies.map {
        mapCurrency(it)
    }.toImmutableList()

    // ── Label Building ─────────────────────────────────────────────────────

    fun buildExchangeRateLabel(groupCurrency: CurrencyUiModel, selectedCurrency: CurrencyUiModel): String =
        resourceProvider.getString(
            R.string.withdrawal_rate_label_format,
            groupCurrency.displayText,
            selectedCurrency.displayText
        )

    fun buildDeductedAmountLabel(groupCurrency: CurrencyUiModel): String = resourceProvider.getString(
        R.string.withdrawal_deducted_label_format,
        groupCurrency.displayText
    )

    fun buildFeeConvertedLabel(groupCurrency: CurrencyUiModel): String = resourceProvider.getString(
        R.string.withdrawal_fee_converted_hint,
        groupCurrency.displayText
    )
}
