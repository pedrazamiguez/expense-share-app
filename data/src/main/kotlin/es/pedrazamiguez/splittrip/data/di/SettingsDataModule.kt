package es.pedrazamiguez.splittrip.data.di

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.data.repository.impl.BalancePreferenceRepositoryImpl
import es.pedrazamiguez.splittrip.data.repository.impl.GroupPreferenceRepositoryImpl
import es.pedrazamiguez.splittrip.data.repository.impl.OnboardingPreferenceRepositoryImpl
import es.pedrazamiguez.splittrip.data.repository.impl.UserPreferenceRepositoryImpl
import es.pedrazamiguez.splittrip.domain.repository.BalancePreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.OnboardingPreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository
import org.koin.dsl.module

val settingsDataModule = module {
    single<OnboardingPreferenceRepository> {
        OnboardingPreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }

    single<GroupPreferenceRepository> {
        GroupPreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }

    single<UserPreferenceRepository> {
        UserPreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }

    single<BalancePreferenceRepository> {
        BalancePreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }
}
