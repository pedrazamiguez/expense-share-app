package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.core.common.di.coreCommonModule
import es.pedrazamiguez.expenseshareapp.core.designsystem.di.coreDesignSystemModule
import org.koin.dsl.module

val coreModules = module {
    includes(
        coreCommonModule, coreDesignSystemModule
    )
}
