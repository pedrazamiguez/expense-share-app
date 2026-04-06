package es.pedrazamiguez.splittrip.di

import es.pedrazamiguez.splittrip.data.di.dataCommonModule
import es.pedrazamiguez.splittrip.data.firebase.di.dataFirebaseModule
import es.pedrazamiguez.splittrip.data.local.di.dataLocalModule
import es.pedrazamiguez.splittrip.data.remote.di.dataRemoteModule
import org.koin.dsl.module

val dataModules = module {
    includes(
        dataFirebaseModule,
        dataLocalModule,
        dataRemoteModule,
        dataCommonModule
    )
}
