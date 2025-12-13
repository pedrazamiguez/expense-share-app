package es.pedrazamiguez.expenseshareapp.core.designsystem.extension

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.core.designsystem.R
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency

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
}
