package es.pedrazamiguez.expenseshareapp.core.common.provider

import java.util.Locale

interface LocaleProvider {
    fun getCurrentLocale(): Locale
}
