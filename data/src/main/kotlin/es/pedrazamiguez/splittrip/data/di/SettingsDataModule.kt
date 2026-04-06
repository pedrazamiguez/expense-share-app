package es.pedrazamiguez.splittrip.data.di

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.data.repository.impl.BalancePreferenceRepositoryImpl
import es.pedrazamiguez.splittrip.data.repository.impl.PreferenceRepositoryImpl
import es.pedrazamiguez.splittrip.domain.repository.BalancePreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository
import org.koin.dsl.module

val settingsDataModule = module {
    single<PreferenceRepository> {
        PreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }

    single<BalancePreferenceRepository> {
        BalancePreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }
}
