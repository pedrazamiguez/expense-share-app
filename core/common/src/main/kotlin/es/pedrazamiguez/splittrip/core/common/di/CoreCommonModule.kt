package es.pedrazamiguez.splittrip.core.common.di

import es.pedrazamiguez.splittrip.core.common.network.NetworkMonitor
import es.pedrazamiguez.splittrip.core.common.network.impl.NetworkMonitorImpl
import es.pedrazamiguez.splittrip.core.common.provider.AppMetadataProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.common.provider.SupportEmailAddressProvider
import es.pedrazamiguez.splittrip.core.common.provider.SupportEmailProvider
import es.pedrazamiguez.splittrip.core.common.provider.impl.SupportEmailProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreCommonModule = module {
    single<SupportEmailProvider> {
        val appMetadataProvider = get<AppMetadataProvider>()
        val resourceProvider = get<ResourceProvider>()
        val supportEmailAddressProvider = get<SupportEmailAddressProvider>()
        SupportEmailProviderImpl(
            appMetadataProvider = appMetadataProvider,
            resourceProvider = resourceProvider,
            supportEmailAddressProvider = supportEmailAddressProvider
        )
    }

    single<NetworkMonitor> {
        NetworkMonitorImpl(context = androidContext())
    }
}
