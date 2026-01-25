package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.AppMetadataProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.provider.IntentProvider
import es.pedrazamiguez.expenseshareapp.provider.impl.AppMetadataProviderImpl
import es.pedrazamiguez.expenseshareapp.provider.impl.IntentProviderImpl
import es.pedrazamiguez.expenseshareapp.provider.impl.LocaleProviderImpl
import es.pedrazamiguez.expenseshareapp.provider.impl.ResourceProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<AppMetadataProvider> { AppMetadataProviderImpl(context = androidContext()) }
    single<IntentProvider> { IntentProviderImpl(context = androidContext()) }
    single<LocaleProvider> { LocaleProviderImpl(context = androidContext()) }
    single<ResourceProvider> { ResourceProviderImpl(context = androidContext()) }
}
