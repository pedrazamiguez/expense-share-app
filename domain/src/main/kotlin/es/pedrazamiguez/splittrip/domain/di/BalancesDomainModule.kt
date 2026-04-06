package es.pedrazamiguez.splittrip.domain.di

import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.service.AddOnCalculationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import org.koin.dsl.module

val balancesDomainModule = module {

    factory {
        GetGroupContributionsFlowUseCase(
            contributionRepository = get<ContributionRepository>()
        )
    }

    factory {
        GetGroupPocketBalanceFlowUseCase(
            contributionRepository = get<ContributionRepository>(),
            expenseRepository = get<ExpenseRepository>(),
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            addOnCalculationService = get<AddOnCalculationService>()
        )
    }

    factory {
        GetCashWithdrawalsFlowUseCase(
            cashWithdrawalRepository = get<CashWithdrawalRepository>()
        )
    }

    factory {
        GetMemberBalancesFlowUseCase(
            addOnCalculationService = get<AddOnCalculationService>()
        )
    }
}
