package es.pedrazamiguez.expenseshareapp.features.contribution.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.contribution.navigation.impl.ContributionsTabGraphContributorImpl
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.mapper.AddContributionUiMapper
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.screen.impl.AddContributionScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel.AddContributionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val contributionsUiModule = module {

    single {
        AddContributionUiMapper(
            localeProvider = get<LocaleProvider>()
        )
    }

    viewModel {
        val addContributionUiMapper = get<AddContributionUiMapper>()

        AddContributionViewModel(
            addContributionUseCase = get<AddContributionUseCase>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
            getGroupSubunitsUseCase = get<GetGroupSubunitsUseCase>(),
            getMemberProfilesUseCase = get<GetMemberProfilesUseCase>(),
            authenticationService = get<AuthenticationService>(),
            contributionValidationService = get<ContributionValidationService>(),
            addContributionUiMapper = addContributionUiMapper
        )
    }

    factory { ContributionsTabGraphContributorImpl() } bind TabGraphContributor::class
    single { AddContributionScreenUiProviderImpl() } bind ScreenUiProvider::class
}
