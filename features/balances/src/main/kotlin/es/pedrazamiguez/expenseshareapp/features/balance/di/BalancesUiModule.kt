package es.pedrazamiguez.expenseshareapp.features.balance.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.navigation.impl.BalancesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.AddCashWithdrawalScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.BalancesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.AddCashWithdrawalViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val balancesUiModule = module {

    single {
        BalancesUiMapper(
            localeProvider = get<LocaleProvider>()
        )
    }

    single {
        AddCashWithdrawalUiMapper(
            localeProvider = get<LocaleProvider>(),
            resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        BalancesViewModel(
            getGroupPocketBalanceFlowUseCase = get<GetGroupPocketBalanceFlowUseCase>(),
            getGroupContributionsFlowUseCase = get<GetGroupContributionsFlowUseCase>(),
            getCashWithdrawalsFlowUseCase = get<GetCashWithdrawalsFlowUseCase>(),
            addContributionUseCase = get<AddContributionUseCase>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
            authenticationService = get<AuthenticationService>(),
            contributionValidationService = get<ContributionValidationService>(),
            balancesUiMapper = get<BalancesUiMapper>(),
            getLastSeenBalanceUseCase = get<GetLastSeenBalanceUseCase>(),
            setLastSeenBalanceUseCase = get<SetLastSeenBalanceUseCase>()
        )
    }

    viewModel {
        AddCashWithdrawalViewModel(
            addCashWithdrawalUseCase = get<AddCashWithdrawalUseCase>(),
            getGroupExpenseConfigUseCase = get<GetGroupExpenseConfigUseCase>(),
            getExchangeRateUseCase = get<GetExchangeRateUseCase>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            cashWithdrawalValidationService = get<CashWithdrawalValidationService>(),
            mapper = get<AddCashWithdrawalUiMapper>()
        )
    }

    factory { BalancesNavigationProviderImpl() } bind NavigationProvider::class
    single { BalancesScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddCashWithdrawalScreenUiProviderImpl() } bind ScreenUiProvider::class
}
