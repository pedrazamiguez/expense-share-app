package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import org.koin.dsl.module

val withdrawalsDomainModule = module {
    factory { CashWithdrawalValidationService() }

    factory {
        AddCashWithdrawalUseCase(
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            validationService = get<CashWithdrawalValidationService>(),
            groupMembershipService = get<GroupMembershipService>(),
            subunitRepository = get<SubunitRepository>(),
            authenticationService = get<AuthenticationService>()
        )
    }
}
