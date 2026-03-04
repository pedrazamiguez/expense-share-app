package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import org.koin.dsl.module

val balancesDomainModule = module {
    factory { ContributionValidationService() }

    factory {
        AddContributionUseCase(
            contributionRepository = get<ContributionRepository>()
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
            expenseRepository = get<ExpenseRepository>()
        )
    }
}
