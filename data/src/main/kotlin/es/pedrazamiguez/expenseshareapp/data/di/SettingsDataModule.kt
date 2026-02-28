package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.data.repository.impl.PreferenceRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import org.koin.dsl.module

val settingsDataModule = module {
    single<PreferenceRepository> {
        PreferenceRepositoryImpl(userPreferences = get<UserPreferences>())
    }
}
