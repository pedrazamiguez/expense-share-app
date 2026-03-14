package es.pedrazamiguez.expenseshareapp.domain.usecase.currency

import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.result.ExchangeRateResult
import java.math.BigDecimal
import java.math.MathContext

class GetExchangeRateUseCase(private val currencyRepository: CurrencyRepository) {
    /**
     * Calculates the cross-rate: 1 [baseCurrencyCode] = X [targetCurrencyCode].
     * Uses USD as pivot to support OpenExchangeRates Free Tier.
     */
    suspend operator fun invoke(baseCurrencyCode: String, targetCurrencyCode: String): BigDecimal? {
        // Free tier only allows fetching USD base.
        val result = currencyRepository.getExchangeRates("USD")

        val ratesList = when (result) {
            is ExchangeRateResult.Fresh -> result.exchangeRates.exchangeRates
            is ExchangeRateResult.Stale -> result.exchangeRates.exchangeRates
            ExchangeRateResult.Empty -> return null
        }

        // Convert list to map for efficient lookup
        val ratesMap = ratesList.associate { it.currency.code to it.rate }

        // Helper to get rate safely (USD itself is 1.0)
        fun getUsdRate(code: String): BigDecimal? = if (code == "USD") BigDecimal.ONE else ratesMap[code]

        val usdToBase = getUsdRate(baseCurrencyCode) ?: return null
        val usdToTarget = getUsdRate(targetCurrencyCode) ?: return null

        if (usdToBase.compareTo(BigDecimal.ZERO) == 0) return null

        // Triangulation: TargetRate / BaseRate
        // Example: 1 USD = 0.9 EUR; 1 USD = 30 THB
        // 1 EUR = 30 / 0.9 = 33.33 THB
        return usdToTarget.divide(usdToBase, MathContext.DECIMAL64)
    }
}
