package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.core.ui.provider.IntentProvider
import es.pedrazamiguez.expenseshareapp.provider.impl.IntentProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<IntentProvider> { IntentProviderImpl(context = androidContext()) }
}
