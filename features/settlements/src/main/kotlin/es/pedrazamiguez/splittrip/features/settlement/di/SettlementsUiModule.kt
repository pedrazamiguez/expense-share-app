package es.pedrazamiguez.splittrip.features.settlement.di

import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.features.settlement.presentation.screen.impl.MyStatusScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val settlementsUiModule = module {
    single { MyStatusScreenUiProviderImpl() } bind ScreenUiProvider::class
}
