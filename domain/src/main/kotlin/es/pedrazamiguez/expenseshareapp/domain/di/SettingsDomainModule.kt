package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.BalancePreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupNameUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.IsOnboardingCompleteUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetOnboardingCompleteUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetSelectedGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetUserDefaultCurrencyUseCase
import org.koin.dsl.module

val settingsDomainModule = module {

    factory<IsOnboardingCompleteUseCase> {
        IsOnboardingCompleteUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<SetOnboardingCompleteUseCase> {
        SetOnboardingCompleteUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<GetSelectedGroupIdUseCase> {
        GetSelectedGroupIdUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<GetSelectedGroupNameUseCase> {
        GetSelectedGroupNameUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<SetSelectedGroupUseCase> {
        SetSelectedGroupUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

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

    factory<GetGroupLastUsedPaymentMethodUseCase> {
        GetGroupLastUsedPaymentMethodUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<SetGroupLastUsedPaymentMethodUseCase> {
        SetGroupLastUsedPaymentMethodUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<GetGroupLastUsedCategoryUseCase> {
        GetGroupLastUsedCategoryUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<SetGroupLastUsedCategoryUseCase> {
        SetGroupLastUsedCategoryUseCase(
            preferenceRepository = get<PreferenceRepository>()
        )
    }

    factory<GetLastSeenBalanceUseCase> {
        GetLastSeenBalanceUseCase(
            balancePreferenceRepository = get<BalancePreferenceRepository>()
        )
    }

    factory<SetLastSeenBalanceUseCase> {
        SetLastSeenBalanceUseCase(
            balancePreferenceRepository = get<BalancePreferenceRepository>()
        )
    }
}
