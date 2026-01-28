package es.pedrazamiguez.expenseshareapp.domain.usecase.currency

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository

/**
 * Use case for fetching supported currencies with common currencies prioritized.
 *
 * This encapsulates the business logic for:
 * - Fetching all available currencies from the repository
 * - Sorting them with commonly used currencies first
 * - Providing a consistent ordering across all screens
 */
class GetSupportedCurrenciesUseCase(
    private val currencyRepository: CurrencyRepository
) {
    companion object {
        /**
         * Common currencies prioritized at the top of the list.
         * Order represents user preference priority based on global usage.
         */
        internal val PREFERRED_CURRENCY_ORDER = listOf(
            "EUR", "USD", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "INR", "MXN",
            "BRL", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "RUB"
        )
    }

    /**
     * Fetches all supported currencies, sorted with common currencies first.
     *
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     * @return Result containing sorted list of currencies, or failure if fetch fails
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<Currency>> {
        return runCatching {
            val currencies = currencyRepository.getCurrencies(forceRefresh)
            sortCurrenciesWithCommonFirst(currencies)
        }
    }

    /**
     * Sorts currencies with common ones first (in preferred order), then others alphabetically.
     */
    private fun sortCurrenciesWithCommonFirst(currencies: List<Currency>): List<Currency> {
        val (common, others) = currencies.partition { it.code in PREFERRED_CURRENCY_ORDER }
        val sortedCommon = common.sortedBy { PREFERRED_CURRENCY_ORDER.indexOf(it.code) }
        val sortedOthers = others.sortedBy { it.code }
        return sortedCommon + sortedOthers
    }
}
