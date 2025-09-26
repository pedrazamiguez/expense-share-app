package es.pedrazamiguez.expenseshareapp.core.config.di

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import org.koin.dsl.module

val coreConfigModule = module {
    single<UserPreferences> { UserPreferences(context = get<Context>()) }
}
