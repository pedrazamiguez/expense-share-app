package es.pedrazamiguez.splittrip.core.common.provider

import java.util.Locale

interface LocaleProvider {
    fun getCurrentLocale(): Locale
}
