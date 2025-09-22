package es.pedrazamiguez.expenseshareapp.core.config.di

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import org.koin.dsl.module
import timber.log.Timber

val coreConfigModule = module {
    single<Timber.Tree> { Timber.asTree() }
    single<UserPreferences> { UserPreferences(context = get<Context>()) }
}
