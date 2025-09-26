package es.pedrazamiguez.expenseshareapp.ui.group.di

import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.impl.GroupsNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.impl.GroupsScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val groupsUiModule = module {
    factory { GroupsNavigationProviderImpl() } bind NavigationProvider::class
    single { GroupsScreenUiProviderImpl() } bind ScreenUiProvider::class
}
