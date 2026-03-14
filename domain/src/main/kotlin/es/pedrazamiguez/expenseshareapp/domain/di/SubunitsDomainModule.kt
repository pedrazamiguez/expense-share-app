package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.service.SubunitValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.DeleteSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import org.koin.dsl.module

val subunitsDomainModule = module {
    factory { SubunitValidationService() }
    factory {
        CreateSubunitUseCase(
            subunitRepository = get<SubunitRepository>(),
            groupRepository = get<GroupRepository>(),
            groupMembershipService = get<GroupMembershipService>(),
            subunitValidationService = get<SubunitValidationService>()
        )
    }
    factory {
        GetGroupSubunitsFlowUseCase(
            subunitRepository = get<SubunitRepository>()
        )
    }
    factory {
        GetGroupSubunitsUseCase(
            subunitRepository = get<SubunitRepository>()
        )
    }
    factory {
        UpdateSubunitUseCase(
            subunitRepository = get<SubunitRepository>(),
            groupRepository = get<GroupRepository>(),
            groupMembershipService = get<GroupMembershipService>(),
            subunitValidationService = get<SubunitValidationService>()
        )
    }
    factory {
        DeleteSubunitUseCase(
            subunitRepository = get<SubunitRepository>(),
            groupMembershipService = get<GroupMembershipService>()
        )
    }
}

