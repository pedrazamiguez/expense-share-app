package es.pedrazamiguez.expenseshareapp.provider.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import java.util.Locale

class LocaleProviderImpl(
    private val context: Context
) : LocaleProvider {

    override fun getCurrentLocale(): Locale = context.resources.configuration.locales[0]

}
