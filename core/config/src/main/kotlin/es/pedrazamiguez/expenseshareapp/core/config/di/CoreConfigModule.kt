package es.pedrazamiguez.expenseshareapp.core.config.di

import org.koin.dsl.module
import timber.log.Timber

val coreConfigModule = module {
    single<Timber.Tree> { Timber.asTree() }
}
