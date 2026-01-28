package es.pedrazamiguez.expenseshareapp.features.group.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.group.navigation.impl.GroupsNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.impl.CreateGroupScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.impl.GroupsScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.CreateGroupViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.ListUserGroupsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val groupsUiModule = module {
    single {
        GroupUiMapper(
            localeProvider = get<LocaleProvider>(), resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        CreateGroupViewModel(
            createGroupUseCase = get<CreateGroupUseCase>(),
            currencyRepository = get<CurrencyRepository>()
        )
    }
    viewModel {
        ListUserGroupsViewModel(
            getUserGroupsFlowUseCase = get<GetUserGroupsFlowUseCase>(),
            groupUiMapper = get<GroupUiMapper>()
        )
    }

    factory { GroupsNavigationProviderImpl() } bind NavigationProvider::class

    single { GroupsScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { CreateGroupScreenUiProviderImpl() } bind ScreenUiProvider::class
}
