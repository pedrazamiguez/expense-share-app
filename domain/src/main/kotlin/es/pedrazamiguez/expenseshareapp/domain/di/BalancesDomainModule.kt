package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import org.koin.dsl.module

val balancesDomainModule = module {
    factory { ContributionValidationService() }
    factory { CashWithdrawalValidationService() }

    factory {
        AddContributionUseCase(
            contributionRepository = get<ContributionRepository>(),
            groupMembershipService = get<GroupMembershipService>()
        )
    }

    factory {
        GetGroupContributionsFlowUseCase(
            contributionRepository = get<ContributionRepository>()
        )
    }

    factory {
        GetGroupPocketBalanceFlowUseCase(
            contributionRepository = get<ContributionRepository>(),
            expenseRepository = get<ExpenseRepository>(),
            cashWithdrawalRepository = get<CashWithdrawalRepository>()
        )
    }

    factory {
        AddCashWithdrawalUseCase(
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            validationService = get<CashWithdrawalValidationService>(),
            groupMembershipService = get<GroupMembershipService>()
        )
    }

    factory {
        GetCashWithdrawalsFlowUseCase(
            cashWithdrawalRepository = get<CashWithdrawalRepository>()
        )
    }
}
