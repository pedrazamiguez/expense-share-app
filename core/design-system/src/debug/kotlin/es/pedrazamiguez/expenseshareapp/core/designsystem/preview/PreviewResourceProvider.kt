package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider

class PreviewResourceProvider(private val context: Context) : ResourceProvider {

    override fun getString(stringResId: Int): String = context.getString(stringResId)

    override fun getString(stringResId: Int, vararg args: Any): String = context.getString(stringResId, *args)

    override fun getQuantityString(pluralResId: Int, quantity: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(pluralResId, quantity, *formatArgs)
}
