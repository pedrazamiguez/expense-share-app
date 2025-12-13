package es.pedrazamiguez.expenseshareapp.features.profile.di

import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.features.profile.navigation.impl.ProfileNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.screen.impl.ProfileScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val profileUiModule = module {
    factory { ProfileNavigationProviderImpl() } bind NavigationProvider::class
    single { ProfileScreenUiProviderImpl() } bind ScreenUiProvider::class
}
