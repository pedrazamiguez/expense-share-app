package es.pedrazamiguez.expenseshareapp.features.group.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.EmailValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetSupportedCurrenciesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.DeleteGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.DeleteSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.SearchUsersByEmailUseCase
import es.pedrazamiguez.expenseshareapp.features.group.navigation.impl.GroupsNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.impl.GroupUiMapperImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.impl.SubunitUiMapperImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.impl.CreateEditSubunitScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.impl.CreateGroupScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.impl.GroupsScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.impl.SubunitManagementScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.CreateEditSubunitViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.CreateGroupViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.GroupsViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.SubunitManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val groupsUiModule = module {

    single<GroupUiMapper> {
        GroupUiMapperImpl(
            localeProvider = get<LocaleProvider>(), resourceProvider = get<ResourceProvider>()
        )
    }

    single<SubunitUiMapper> {
        SubunitUiMapperImpl(
            localeProvider = get<LocaleProvider>(), resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        CreateGroupViewModel(
            createGroupUseCase = get<CreateGroupUseCase>(),
            getSupportedCurrenciesUseCase = get<GetSupportedCurrenciesUseCase>(),
            getUserDefaultCurrencyUseCase = get<GetUserDefaultCurrencyUseCase>(),
            searchUsersByEmailUseCase = get<SearchUsersByEmailUseCase>(),
            emailValidationService = get<EmailValidationService>(),
        )
    }

    viewModel {
        GroupsViewModel(
            getUserGroupsFlowUseCase = get<GetUserGroupsFlowUseCase>(),
            deleteGroupUseCase = get<DeleteGroupUseCase>(),
            groupUiMapper = get<GroupUiMapper>()
        )
    }

    viewModel {
        SubunitManagementViewModel(
            getGroupSubunitsFlowUseCase = get<GetGroupSubunitsFlowUseCase>(),
            deleteSubunitUseCase = get<DeleteSubunitUseCase>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
            getMemberProfilesUseCase = get<GetMemberProfilesUseCase>(),
            subunitUiMapper = get<SubunitUiMapper>()
        )
    }

    viewModel {
        CreateEditSubunitViewModel(
            createSubunitUseCase = get<CreateSubunitUseCase>(),
            updateSubunitUseCase = get<UpdateSubunitUseCase>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
            getGroupSubunitsFlowUseCase = get<GetGroupSubunitsFlowUseCase>(),
            getMemberProfilesUseCase = get<GetMemberProfilesUseCase>(),
            subunitUiMapper = get<SubunitUiMapper>()
        )
    }

    factory { GroupsNavigationProviderImpl() } bind NavigationProvider::class

    single { GroupsScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { CreateGroupScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { SubunitManagementScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { CreateEditSubunitScreenUiProviderImpl() } bind ScreenUiProvider::class
}
