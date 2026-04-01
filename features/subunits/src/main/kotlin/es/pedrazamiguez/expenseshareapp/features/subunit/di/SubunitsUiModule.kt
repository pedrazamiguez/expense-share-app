package es.pedrazamiguez.expenseshareapp.features.subunit.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.SubunitShareDistributionService
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.DeleteSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.subunit.navigation.impl.SubunitsTabGraphContributorImpl
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.mapper.impl.SubunitUiMapperImpl
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.screen.impl.CreateEditSubunitScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.screen.impl.SubunitManagementScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.CreateEditSubunitViewModel
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.SubunitManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val subunitsUiModule = module {

    single<SubunitUiMapper> {
        SubunitUiMapperImpl(
            localeProvider = get<LocaleProvider>(),
            resourceProvider = get<ResourceProvider>()
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
            subunitUiMapper = get<SubunitUiMapper>(),
            shareDistributionService = get<SubunitShareDistributionService>()
        )
    }

    factory { SubunitsTabGraphContributorImpl() } bind TabGraphContributor::class
    single { SubunitManagementScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { CreateEditSubunitScreenUiProviderImpl() } bind ScreenUiProvider::class
}
