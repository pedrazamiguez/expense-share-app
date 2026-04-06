package es.pedrazamiguez.splittrip.provider.impl

import android.content.Context
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider

class ResourceProviderImpl(private val context: Context) : ResourceProvider {

    override fun getString(stringResId: Int): String = context.getString(stringResId)

    override fun getString(stringResId: Int, vararg args: Any): String = context.getString(stringResId, *args)

    override fun getQuantityString(pluralResId: Int, quantity: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(pluralResId, quantity, *formatArgs)
}
