package es.pedrazamiguez.expenseshareapp.core.config.di

import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreConfigModule = module {
    single<UserPreferences> { UserPreferences(context = androidContext()) }
}
