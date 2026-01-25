package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.data.di.dataCommonModule
import es.pedrazamiguez.expenseshareapp.data.firebase.di.dataFirebaseModule
import es.pedrazamiguez.expenseshareapp.data.local.di.dataLocalModule
import es.pedrazamiguez.expenseshareapp.data.remote.di.dataRemoteModule
import org.koin.dsl.module

val dataModules = module {
    includes(
        dataFirebaseModule, dataLocalModule, dataRemoteModule, dataCommonModule
    )
}
