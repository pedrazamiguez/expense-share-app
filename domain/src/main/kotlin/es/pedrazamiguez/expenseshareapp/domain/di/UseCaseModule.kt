package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.GetUserBalanceUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory<GetUserBalanceUseCase> { GetUserBalanceUseCase(userRepository = get<UserRepository>()) }
}
