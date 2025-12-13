package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.core.designsystem.provider.IntentProvider
import es.pedrazamiguez.expenseshareapp.domain.provider.AppMetadataProvider
import es.pedrazamiguez.expenseshareapp.provider.impl.AppMetadataProviderImpl
import es.pedrazamiguez.expenseshareapp.provider.impl.IntentProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<AppMetadataProvider> { AppMetadataProviderImpl(context = androidContext()) }
    single<IntentProvider> { IntentProviderImpl(context = androidContext()) }
}
