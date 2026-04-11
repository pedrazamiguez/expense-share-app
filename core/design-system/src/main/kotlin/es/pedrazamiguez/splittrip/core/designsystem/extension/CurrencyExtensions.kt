package es.pedrazamiguez.splittrip.core.designsystem.extension

import androidx.annotation.StringRes
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.domain.enums.Currency
import es.pedrazamiguez.splittrip.domain.model.Currency as CurrencyModel

/**
 * Maps [Currency] to its string resource ID.
 *
 * A `when` expression is used instead of a `Map` to preserve the Kotlin compiler's
 * exhaustiveness check: adding a new [Currency] entry without a corresponding branch
 * causes a compile error, preventing silent runtime crashes. The cyclomatic complexity
 * warning is suppressed because all branches are simple enum-to-resource-ID mappings
 * with no procedural logic.
 */
@Suppress("CyclomaticComplexMethod") // Exhaustive enum mapping — each branch is a single constant lookup
@StringRes
fun Currency.getNameRes(): Int = when (this) {
    Currency.EUR -> R.string.currency_name_eur
    Currency.USD -> R.string.currency_name_usd
    Currency.GBP -> R.string.currency_name_gbp
    Currency.MXN -> R.string.currency_name_mxn
    Currency.JPY -> R.string.currency_name_jpy
    Currency.AUD -> R.string.currency_name_aud
    Currency.CAD -> R.string.currency_name_cad
    Currency.CHF -> R.string.currency_name_chf
    Currency.CNY -> R.string.currency_name_cny
    Currency.SEK -> R.string.currency_name_sek
    Currency.NZD -> R.string.currency_name_nzd
    Currency.THB -> R.string.currency_name_thb
    Currency.KRW -> R.string.currency_name_krw
    Currency.INR -> R.string.currency_name_inr
    Currency.BRL -> R.string.currency_name_brl
    Currency.ARS -> R.string.currency_name_ars
    Currency.COP -> R.string.currency_name_cop
    Currency.CLP -> R.string.currency_name_clp
    Currency.PEN -> R.string.currency_name_pen
    Currency.DKK -> R.string.currency_name_dkk
    Currency.NOK -> R.string.currency_name_nok
    Currency.PLN -> R.string.currency_name_pln
    Currency.CZK -> R.string.currency_name_czk
    Currency.HUF -> R.string.currency_name_huf
    Currency.TRY -> R.string.currency_name_try
    Currency.MAD -> R.string.currency_name_mad
    Currency.AED -> R.string.currency_name_aed
    Currency.ZAR -> R.string.currency_name_zar
    Currency.SGD -> R.string.currency_name_sgd
    Currency.HKD -> R.string.currency_name_hkd
    Currency.IDR -> R.string.currency_name_idr
}

/**
 * Resolves the locale-aware display name for this [CurrencyModel] using Android string resources.
 *
 * For currencies that exist in [Currency] (the domain enum), the name is resolved via
 * [getNameRes] and [ResourceProvider], picking up the current device/app locale automatically.
 * For currencies not present in the enum (e.g. exotic API-only codes), the function falls back
 * to [CurrencyModel.defaultName] (the English name stored from the Open Exchange Rates API).
 *
 * @param resourceProvider Used to retrieve the locale-aware string resource.
 * @return The localized name, or [CurrencyModel.defaultName] if the currency is not in the enum.
 */
fun CurrencyModel.resolveLocalizedName(resourceProvider: ResourceProvider): String =
    Currency.entries
        .find { it.name.equals(code, ignoreCase = true) }
        ?.let { resourceProvider.getString(it.getNameRes()) }
        ?: defaultName
