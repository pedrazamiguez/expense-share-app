package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import org.koin.dsl.module

val groupsDomainModule = module {
    factory<CreateGroupUseCase> { CreateGroupUseCase(groupRepository = get<GroupRepository>()) }
    factory<GetUserGroupsFlowUseCase> { GetUserGroupsFlowUseCase(groupRepository = get<GroupRepository>()) }
}
