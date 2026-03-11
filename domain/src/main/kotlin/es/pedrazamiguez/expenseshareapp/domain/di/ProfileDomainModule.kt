package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import org.koin.dsl.module

val profileDomainModule = module {
    factory {
        GetMemberProfilesUseCase(
            userRepository = get<UserRepository>()
        )
    }
}
