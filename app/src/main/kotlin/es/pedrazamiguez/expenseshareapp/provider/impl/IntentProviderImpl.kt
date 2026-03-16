package es.pedrazamiguez.expenseshareapp.provider.impl

import android.content.Context
import android.content.Intent
import es.pedrazamiguez.expenseshareapp.MainActivity
import es.pedrazamiguez.expenseshareapp.core.designsystem.provider.IntentProvider

class IntentProviderImpl(private val context: Context) : IntentProvider {

    override val targetActivityClassName: String = MainActivity::class.java.name

    override fun getMainIntent(): Intent = Intent(context, MainActivity::class.java)
}
