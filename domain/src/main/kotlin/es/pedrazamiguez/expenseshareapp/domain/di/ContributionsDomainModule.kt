package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import org.koin.dsl.module

val contributionsDomainModule = module {
    factory { ContributionValidationService() }

    factory {
        AddContributionUseCase(
            contributionRepository = get<ContributionRepository>(),
            groupMembershipService = get<GroupMembershipService>(),
            contributionValidationService = get<ContributionValidationService>(),
            subunitRepository = get<SubunitRepository>(),
            authenticationService = get<AuthenticationService>()
        )
    }
}
