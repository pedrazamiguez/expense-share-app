package es.pedrazamiguez.expenseshareapp.ui.group.di

import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.impl.GroupsNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.impl.CreateGroupScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.impl.GroupsScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.viewmodel.CreateGroupViewModel
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.viewmodel.ListUserGroupsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val groupsUiModule = module {
    viewModel { CreateGroupViewModel(createGroupUseCase = get<CreateGroupUseCase>()) }
    viewModel {
        ListUserGroupsViewModel(
            getUserGroupsFlowUseCase = get<GetUserGroupsFlowUseCase>()
        )
    }

    factory { GroupsNavigationProviderImpl() } bind NavigationProvider::class

    single { GroupsScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { CreateGroupScreenUiProviderImpl() } bind ScreenUiProvider::class
}
