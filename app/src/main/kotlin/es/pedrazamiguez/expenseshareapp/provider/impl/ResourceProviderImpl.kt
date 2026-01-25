package es.pedrazamiguez.expenseshareapp.provider.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider

class ResourceProviderImpl(
    private val context: Context
) : ResourceProvider {

    override fun getString(stringResId: Int): String {
        return context.getString(stringResId)
    }

    override fun getString(stringResId: Int, vararg args: Any): String {
        return context.getString(stringResId, *args)
    }

}
