package es.pedrazamiguez.expenseshareapp.features.balance.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.navigation.impl.BalancesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.AddCashWithdrawalScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.AddContributionScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.BalancesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.AddCashWithdrawalViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.AddContributionViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesUseCases
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalConfigHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalCurrencyHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalFeeHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalSubmitHandler
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

    single {
        AddCashWithdrawalUiMapper(
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

    // ── AddCashWithdrawal ViewModel with co-created handlers ─────────
    // Handlers are created inside the viewModel block so the SAME instances
    // are shared between the ViewModel and any cross-handler references.

    viewModel {
        val addCashWithdrawalUiMapper = get<AddCashWithdrawalUiMapper>()
        val formattingHelper = get<FormattingHelper>()

        val configHandler = WithdrawalConfigHandler(
            getGroupExpenseConfigUseCase = get<GetGroupExpenseConfigUseCase>(),
            getGroupSubunitsUseCase = get<GetGroupSubunitsUseCase>(),
            authenticationService = get<AuthenticationService>(),
            addCashWithdrawalUiMapper = addCashWithdrawalUiMapper
        )

        val currencyHandler = WithdrawalCurrencyHandler(
            getExchangeRateUseCase = get<GetExchangeRateUseCase>(),
            exchangeRateCalculationService = get<ExchangeRateCalculationService>(),
            addCashWithdrawalUiMapper = addCashWithdrawalUiMapper,
            formattingHelper = formattingHelper
        )

        val feeHandler = WithdrawalFeeHandler(
            getExchangeRateUseCase = get<GetExchangeRateUseCase>(),
            exchangeRateCalculationService = get<ExchangeRateCalculationService>(),
            addCashWithdrawalUiMapper = addCashWithdrawalUiMapper,
            formattingHelper = formattingHelper
        )

        val submitHandler = WithdrawalSubmitHandler(
            addCashWithdrawalUseCase = get<AddCashWithdrawalUseCase>(),
            cashWithdrawalValidationService = get<CashWithdrawalValidationService>(),
            exchangeRateCalculationService = get<ExchangeRateCalculationService>()
        )

        AddCashWithdrawalViewModel(
            configHandler = configHandler,
            currencyHandler = currencyHandler,
            feeHandler = feeHandler,
            submitHandler = submitHandler
        )
    }

    viewModel {
        AddContributionViewModel(
            addContributionUseCase = get<AddContributionUseCase>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
            getGroupSubunitsUseCase = get<GetGroupSubunitsUseCase>(),
            authenticationService = get<AuthenticationService>(),
            contributionValidationService = get<ContributionValidationService>(),
            balancesUiMapper = get<BalancesUiMapper>()
        )
    }

    factory { BalancesNavigationProviderImpl() } bind NavigationProvider::class
    single { BalancesScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddContributionScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddCashWithdrawalScreenUiProviderImpl() } bind ScreenUiProvider::class
}
