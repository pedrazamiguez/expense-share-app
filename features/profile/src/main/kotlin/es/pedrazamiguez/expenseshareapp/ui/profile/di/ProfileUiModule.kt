package es.pedrazamiguez.expenseshareapp.ui.profile.di

import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.profile.navigation.impl.ProfileNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.profile.presentation.screen.impl.ProfileScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val profileUiModule = module {
    factory { ProfileNavigationProviderImpl() } bind NavigationProvider::class
    single { ProfileScreenUiProviderImpl() } bind ScreenUiProvider::class
}
