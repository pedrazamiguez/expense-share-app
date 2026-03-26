package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.data.repository.impl.BalancePreferenceRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.repository.impl.PreferenceRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.repository.BalancePreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import org.koin.dsl.module

val settingsDataModule = module {
    single<PreferenceRepository> {
        PreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }

    single<BalancePreferenceRepository> {
        BalancePreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }
}
