package es.pedrazamiguez.splittrip.core.designsystem.preview

import android.content.Context
import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import java.util.Locale

class PreviewLocaleProvider(private val context: Context) : LocaleProvider {

    override fun getCurrentLocale(): Locale = context.resources.configuration.locales[0]
}
