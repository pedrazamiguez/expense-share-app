package es.pedrazamiguez.splittrip.features.settlement.di

import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.features.settlement.presentation.screen.impl.MyPositionScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val settlementsUiModule = module {
    single { MyPositionScreenUiProviderImpl() } bind ScreenUiProvider::class
}
