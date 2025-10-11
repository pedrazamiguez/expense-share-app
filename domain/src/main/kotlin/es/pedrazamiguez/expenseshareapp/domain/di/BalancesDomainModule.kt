package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetBalancesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetUserBalanceUseCase
import org.koin.dsl.module

val balancesDomainModule = module {
    factory<GetUserBalanceUseCase> { GetUserBalanceUseCase(userRepository = get<UserRepository>()) }
    factory<GetBalancesUseCase> { GetBalancesUseCase() }
}
