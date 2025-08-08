package es.pedrazamiguez.expenseshareapp.core.di

import org.koin.dsl.module
import timber.log.Timber

val coreModule = module {
    single { Timber.asTree() }
}
