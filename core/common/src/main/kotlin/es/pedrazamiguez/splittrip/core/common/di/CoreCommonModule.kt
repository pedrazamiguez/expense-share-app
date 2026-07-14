package es.pedrazamiguez.splittrip.core.common.di

import es.pedrazamiguez.splittrip.core.common.provider.AppMetadataProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.common.provider.SupportEmailProvider
import es.pedrazamiguez.splittrip.core.common.provider.impl.SupportEmailProviderImpl
import org.koin.dsl.module

val coreCommonModule = module {
    single<SupportEmailProvider> {
        val appMetadataProvider = get<AppMetadataProvider>()
        val resourceProvider = get<ResourceProvider>()
        SupportEmailProviderImpl(
            appMetadataProvider = appMetadataProvider,
            resourceProvider = resourceProvider
        )
    }
}
