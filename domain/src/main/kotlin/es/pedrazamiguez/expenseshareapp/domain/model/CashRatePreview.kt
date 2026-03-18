package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal

/**
 * Preview of the blended exchange rate for a CASH expense,
 * computed from available ATM withdrawals using the FIFO algorithm.
 *
 * @param displayRate The blended display rate (1 GroupCurrency = X SourceCurrency).
 * @param groupAmountCents The equivalent cost in group currency (in cents), or 0 if only
 *                         a weighted-average preview was possible.
 */
data class CashRatePreview(
    val displayRate: BigDecimal,
    val groupAmountCents: Long = 0
)

