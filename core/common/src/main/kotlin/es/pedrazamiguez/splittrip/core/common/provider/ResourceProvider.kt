package es.pedrazamiguez.splittrip.core.common.provider

interface ResourceProvider {
    fun getString(stringResId: Int): String
    fun getString(stringResId: Int, vararg args: Any): String
    fun getQuantityString(pluralResId: Int, quantity: Int, vararg formatArgs: Any): String
}
