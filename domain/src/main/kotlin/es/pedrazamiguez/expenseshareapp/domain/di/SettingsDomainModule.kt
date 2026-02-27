package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import org.koin.dsl.module

val settingsDomainModule = module {

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
