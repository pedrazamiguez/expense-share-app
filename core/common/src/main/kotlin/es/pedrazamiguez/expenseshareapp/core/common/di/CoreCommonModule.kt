package es.pedrazamiguez.expenseshareapp.core.common.di

import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreCommonModule = module {
    single<UserPreferences> { UserPreferences(context = androidContext()) }
}
