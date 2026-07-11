package es.pedrazamiguez.splittrip.di

import es.pedrazamiguez.splittrip.core.common.di.coreCommonModule
import es.pedrazamiguez.splittrip.core.designsystem.di.coreDesignSystemModule
import es.pedrazamiguez.splittrip.core.logging.di.coreLoggingModule
import es.pedrazamiguez.splittrip.core.performance.di.performanceModule
import org.koin.dsl.module

val coreModules = module {
    includes(
        coreCommonModule,
        coreDesignSystemModule,
        coreLoggingModule,
        performanceModule
    )
}
