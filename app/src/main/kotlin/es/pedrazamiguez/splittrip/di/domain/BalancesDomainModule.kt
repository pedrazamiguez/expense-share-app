package es.pedrazamiguez.splittrip.di.domain

import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.AddOnCalculationService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService
import es.pedrazamiguez.splittrip.domain.service.impl.DebtSimplificationServiceImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreGroupSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DeleteCashWithdrawalUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DeleteContributionUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetContributionByExpenseIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.AreGroupSettlementsResolvedUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.AreMemberSettlementsResolvedUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.ConfirmSettlementUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.DeleteCashWithdrawalUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.DeleteContributionUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.DisputeSettlementUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.GetCashWithdrawalsFlowUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.GetContributionByExpenseIdUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.GetGroupContributionsFlowUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.GetGroupPocketBalanceFlowUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.GetMemberBalancesFlowUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.GetSettlementSuggestionsUseCaseImpl
import org.koin.dsl.module

val balancesDomainModule = module {

    factory<GetGroupContributionsFlowUseCase> {
        GetGroupContributionsFlowUseCaseImpl(
            contributionRepository = get<ContributionRepository>()
        )
    }

    factory<GetGroupPocketBalanceFlowUseCase> {
        GetGroupPocketBalanceFlowUseCaseImpl(
            contributionRepository = get<ContributionRepository>(),
            expenseRepository = get<ExpenseRepository>(),
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            addOnCalculationService = get<AddOnCalculationService>()
        )
    }

    factory<GetCashWithdrawalsFlowUseCase> {
        GetCashWithdrawalsFlowUseCaseImpl(
            cashWithdrawalRepository = get<CashWithdrawalRepository>()
        )
    }

    factory<GetMemberBalancesFlowUseCase> {
        GetMemberBalancesFlowUseCaseImpl(
            addOnCalculationService = get<AddOnCalculationService>()
        )
    }

    factory<DeleteContributionUseCase> {
        DeleteContributionUseCaseImpl(
            contributionRepository = get<ContributionRepository>(),
            groupMembershipService = get<GroupMembershipService>(),
            groupRepository = get<GroupRepository>()
        )
    }

    factory<DeleteCashWithdrawalUseCase> {
        DeleteCashWithdrawalUseCaseImpl(
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            groupMembershipService = get<GroupMembershipService>(),
            groupRepository = get<GroupRepository>()
        )
    }

    factory<GetContributionByExpenseIdUseCase> {
        GetContributionByExpenseIdUseCaseImpl(
            contributionRepository = get<ContributionRepository>(),
            groupMembershipService = get<GroupMembershipService>()
        )
    }

    factory<DebtSimplificationService> {
        DebtSimplificationServiceImpl()
    }

    factory<GetSettlementSuggestionsUseCase> {
        GetSettlementSuggestionsUseCaseImpl(
            debtSimplificationService = get<DebtSimplificationService>(),
            settlementRepository = get<SettlementRepository>(),
            groupRepository = get<GroupRepository>(),
            expenseRepository = get<ExpenseRepository>(),
            contributionRepository = get<ContributionRepository>(),
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            subunitRepository = get<SubunitRepository>(),
            getMemberBalancesFlowUseCase = get<GetMemberBalancesFlowUseCase>()
        )
    }

    factory<ConfirmSettlementUseCase> {
        ConfirmSettlementUseCaseImpl(
            settlementRepository = get<SettlementRepository>(),
            authenticationService = get<AuthenticationService>()
        )
    }

    factory<DisputeSettlementUseCase> {
        DisputeSettlementUseCaseImpl(
            settlementRepository = get<SettlementRepository>(),
            authenticationService = get<AuthenticationService>()
        )
    }

    factory<AreMemberSettlementsResolvedUseCase> {
        AreMemberSettlementsResolvedUseCaseImpl(
            settlementRepository = get<SettlementRepository>()
        )
    }

    factory<AreGroupSettlementsResolvedUseCase> {
        AreGroupSettlementsResolvedUseCaseImpl(
            settlementRepository = get<SettlementRepository>()
        )
    }
}
