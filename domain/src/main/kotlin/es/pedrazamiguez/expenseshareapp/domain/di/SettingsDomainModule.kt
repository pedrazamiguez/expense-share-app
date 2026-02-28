package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetUserDefaultCurrencyUseCase
import org.koin.dsl.module

val settingsDomainModule = module {

    factory<GetUserDefaultCurrencyUseCase> {
        GetUserDefaultCurrencyUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<SetUserDefaultCurrencyUseCase> {
        SetUserDefaultCurrencyUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<GetGroupLastUsedCurrencyUseCase> {
        GetGroupLastUsedCurrencyUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<SetGroupLastUsedCurrencyUseCase> {
        SetGroupLastUsedCurrencyUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

}
