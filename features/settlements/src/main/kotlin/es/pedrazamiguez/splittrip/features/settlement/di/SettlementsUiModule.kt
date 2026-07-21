package es.pedrazamiguez.splittrip.features.settlement.di

import es.pedrazamiguez.splittrip.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.features.settlement.navigation.impl.SettlementsTabGraphContributorImpl
import es.pedrazamiguez.splittrip.features.settlement.presentation.screen.impl.MyPositionScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val settlementsUiModule = module {
    factory { SettlementsTabGraphContributorImpl() } bind TabGraphContributor::class
    single { MyPositionScreenUiProviderImpl() } bind ScreenUiProvider::class
}
