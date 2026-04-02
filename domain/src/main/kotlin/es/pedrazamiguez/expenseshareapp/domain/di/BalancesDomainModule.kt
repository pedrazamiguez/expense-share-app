package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AddOnCalculationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetMemberBalancesFlowUseCase
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
