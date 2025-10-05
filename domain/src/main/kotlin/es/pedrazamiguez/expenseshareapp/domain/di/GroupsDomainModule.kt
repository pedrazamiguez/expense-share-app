package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.groups.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.groups.GetUserGroupsUseCase
import org.koin.dsl.module

val groupsDomainModule = module {
    factory<CreateGroupUseCase> { CreateGroupUseCase(groupRepository = get<GroupRepository>()) }
    factory<GetUserGroupsUseCase> { GetUserGroupsUseCase(groupRepository = get<GroupRepository>()) }
}
