package es.pedrazamiguez.expenseshareapp.features.balance.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.navigation.impl.BalancesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.BalancesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesUseCases
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val balancesUiModule = module {

    single {
        BalancesUiMapper(
            localeProvider = get<LocaleProvider>(),
            resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        BalancesViewModel(
            useCases = BalancesUseCases(
                getGroupPocketBalanceFlowUseCase = get<GetGroupPocketBalanceFlowUseCase>(),
                getGroupContributionsFlowUseCase = get<GetGroupContributionsFlowUseCase>(),
                getCashWithdrawalsFlowUseCase = get<GetCashWithdrawalsFlowUseCase>(),
                getGroupExpensesFlowUseCase = get<GetGroupExpensesFlowUseCase>(),
                getMemberBalancesFlowUseCase = get<GetMemberBalancesFlowUseCase>(),
                getGroupSubunitsFlowUseCase = get<GetGroupSubunitsFlowUseCase>(),
                getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
                getLastSeenBalanceUseCase = get<GetLastSeenBalanceUseCase>(),
                setLastSeenBalanceUseCase = get<SetLastSeenBalanceUseCase>(),
                getMemberProfilesUseCase = get<GetMemberProfilesUseCase>()
            ),
            authenticationService = get<AuthenticationService>(),
            balancesUiMapper = get<BalancesUiMapper>()
        )
    }

    factory {
        BalancesNavigationProviderImpl(
            graphContributors = getAll<TabGraphContributor>()
        )
    } bind NavigationProvider::class
    single { BalancesScreenUiProviderImpl() } bind ScreenUiProvider::class
}
