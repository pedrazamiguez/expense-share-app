package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.EmailValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.DeleteGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import org.koin.dsl.module

val groupsDomainModule = module {
    factory {
        GroupMembershipService(
            groupRepository = get<GroupRepository>(),
            authenticationService = get<AuthenticationService>()
        )
    }
    factory<CreateGroupUseCase> { CreateGroupUseCase(groupRepository = get<GroupRepository>()) }
    factory { EmailValidationService() }
    factory<DeleteGroupUseCase> { DeleteGroupUseCase(groupRepository = get<GroupRepository>()) }
    factory<GetGroupByIdUseCase> { GetGroupByIdUseCase(groupRepository = get<GroupRepository>()) }
    factory<GetUserGroupsFlowUseCase> { GetUserGroupsFlowUseCase(groupRepository = get<GroupRepository>()) }
}
