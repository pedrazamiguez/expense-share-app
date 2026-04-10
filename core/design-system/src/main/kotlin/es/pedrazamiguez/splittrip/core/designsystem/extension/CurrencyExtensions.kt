package es.pedrazamiguez.splittrip.core.designsystem.extension

import androidx.annotation.StringRes
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.domain.enums.Currency
import es.pedrazamiguez.splittrip.domain.model.Currency as CurrencyModel

/**
 * Maps each [Currency] enum entry to its string resource ID.
 *
 * Using a `Map` instead of a `when` expression keeps the cyclomatic complexity O(1)
 * regardless of how many currencies are added in the future, and avoids Detekt's
 * `CyclomaticComplexMethod` threshold that a 32-branch `when` would trigger.
 */
private val currencyNameResMap: Map<Currency, Int> = mapOf(
    Currency.EUR to R.string.currency_name_eur,
    Currency.USD to R.string.currency_name_usd,
    Currency.GBP to R.string.currency_name_gbp,
    Currency.MXN to R.string.currency_name_mxn,
    Currency.JPY to R.string.currency_name_jpy,
    Currency.AUD to R.string.currency_name_aud,
    Currency.CAD to R.string.currency_name_cad,
    Currency.CHF to R.string.currency_name_chf,
    Currency.CNY to R.string.currency_name_cny,
    Currency.SEK to R.string.currency_name_sek,
    Currency.NZD to R.string.currency_name_nzd,
    Currency.THB to R.string.currency_name_thb,
    Currency.KRW to R.string.currency_name_krw,
    Currency.INR to R.string.currency_name_inr,
    Currency.BRL to R.string.currency_name_brl,
    Currency.ARS to R.string.currency_name_ars,
    Currency.COP to R.string.currency_name_cop,
    Currency.CLP to R.string.currency_name_clp,
    Currency.PEN to R.string.currency_name_pen,
    Currency.DKK to R.string.currency_name_dkk,
    Currency.NOK to R.string.currency_name_nok,
    Currency.PLN to R.string.currency_name_pln,
    Currency.CZK to R.string.currency_name_czk,
    Currency.HUF to R.string.currency_name_huf,
    Currency.TRY to R.string.currency_name_try,
    Currency.MAD to R.string.currency_name_mad,
    Currency.AED to R.string.currency_name_aed,
    Currency.ZAR to R.string.currency_name_zar,
    Currency.SGD to R.string.currency_name_sgd,
    Currency.HKD to R.string.currency_name_hkd,
    Currency.IDR to R.string.currency_name_idr
)

@StringRes
fun Currency.getNameRes(): Int = currencyNameResMap.getValue(this)

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
