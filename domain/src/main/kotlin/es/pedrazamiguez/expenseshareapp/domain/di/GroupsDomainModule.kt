package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.CreateGroupUseCase
import org.koin.dsl.module

val groupsDomainModule = module {
    factory<CreateGroupUseCase> { CreateGroupUseCase(groupRepository = get<GroupRepository>()) }
}
