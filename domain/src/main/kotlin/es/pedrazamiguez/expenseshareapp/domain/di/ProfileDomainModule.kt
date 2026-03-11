package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberDisplayNamesUseCase
import org.koin.dsl.module

val profileDomainModule = module {
    factory {
        GetMemberDisplayNamesUseCase(
            userRepository = get<UserRepository>()
        )
    }
}
