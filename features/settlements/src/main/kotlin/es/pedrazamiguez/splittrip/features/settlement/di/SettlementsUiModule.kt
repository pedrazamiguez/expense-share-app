package es.pedrazamiguez.splittrip.features.settlement.di

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.domain.service.AppConfigService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.features.settlement.navigation.impl.SettlementsTabGraphContributorImpl
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.MyPositionUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.screen.impl.MyPositionScreenUiProviderImpl
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.MyPositionUseCases
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.MyPositionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val settlementsUiModule = module {
    viewModel {
        val getGroupByIdUseCase = get<GetGroupByIdUseCase>()
        val getGroupContributionsFlowUseCase = get<GetGroupContributionsFlowUseCase>()
        val getCashWithdrawalsFlowUseCase = get<GetCashWithdrawalsFlowUseCase>()
        val getGroupExpensesFlowUseCase = get<GetGroupExpensesFlowUseCase>()
        val getGroupSubunitsFlowUseCase = get<GetGroupSubunitsFlowUseCase>()
        val getMemberBalancesFlowUseCase = get<GetMemberBalancesFlowUseCase>()
        val getGroupSettlementsFlowUseCase = get<GetGroupSettlementsFlowUseCase>()
        val authenticationService = get<AuthenticationService>()
        val appConfigService = get<AppConfigService>()
        val localeProvider = get<LocaleProvider>()

        val myPositionUiMapper = MyPositionUiMapper(
            localeProvider = localeProvider
        )

        val myPositionUseCases = MyPositionUseCases(
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupContributionsFlowUseCase = getGroupContributionsFlowUseCase,
            getCashWithdrawalsFlowUseCase = getCashWithdrawalsFlowUseCase,
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase
        )

        MyPositionViewModel(
            useCases = myPositionUseCases,
            authenticationService = authenticationService,
            myPositionUiMapper = myPositionUiMapper,
            appConfigService = appConfigService
        )
    }
    factory { SettlementsTabGraphContributorImpl() } bind TabGraphContributor::class
    single { MyPositionScreenUiProviderImpl() } bind ScreenUiProvider::class
}
