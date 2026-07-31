package es.pedrazamiguez.splittrip.features.settlement.di

import es.pedrazamiguez.splittrip.core.common.network.NetworkMonitor
import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.domain.service.AppConfigService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.GetNudgeTimestampsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.NudgeDebtorUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.settlement.navigation.impl.SettlementsTabGraphContributorImpl
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.SettlementConsensusUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.YourPositionUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.screen.impl.YourPositionScreenUiProviderImpl
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.YourPositionUseCases
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.YourPositionViewModel
import org.koin.core.module.dsl.viewModel
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
        val confirmSettlementUseCase = get<ConfirmSettlementUseCase>()
        val disputeSettlementUseCase = get<DisputeSettlementUseCase>()
        val getMemberProfilesUseCase = get<GetMemberProfilesUseCase>()
        val getSettlementSuggestionsUseCase = get<GetSettlementSuggestionsUseCase>()
        val nudgeDebtorUseCase = get<NudgeDebtorUseCase>()
        val getNudgeTimestampsFlowUseCase = get<GetNudgeTimestampsFlowUseCase>()
        val authenticationService = get<AuthenticationService>()
        val appConfigService = get<AppConfigService>()
        val networkMonitor = get<NetworkMonitor>()
        val localeProvider = get<LocaleProvider>()
        val resourceProvider = get<ResourceProvider>()

        val yourPositionUiMapper = YourPositionUiMapper(
            localeProvider = localeProvider,
            resourceProvider = resourceProvider
        )

        val settlementConsensusUiMapper = SettlementConsensusUiMapper(
            localeProvider = localeProvider,
            resourceProvider = resourceProvider
        )

        val yourPositionUseCases = YourPositionUseCases(
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupContributionsFlowUseCase = getGroupContributionsFlowUseCase,
            getCashWithdrawalsFlowUseCase = getCashWithdrawalsFlowUseCase,
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase,
            confirmSettlementUseCase = confirmSettlementUseCase,
            disputeSettlementUseCase = disputeSettlementUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase,
            nudgeDebtorUseCase = nudgeDebtorUseCase,
            getNudgeTimestampsFlowUseCase = getNudgeTimestampsFlowUseCase
        )

        YourPositionViewModel(
            useCases = yourPositionUseCases,
            authenticationService = authenticationService,
            yourPositionUiMapper = yourPositionUiMapper,
            settlementConsensusUiMapper = settlementConsensusUiMapper,
            appConfigService = appConfigService,
            networkMonitor = networkMonitor
        )
    }
    factory { SettlementsTabGraphContributorImpl() } bind TabGraphContributor::class
    single { YourPositionScreenUiProviderImpl() } bind ScreenUiProvider::class
}
