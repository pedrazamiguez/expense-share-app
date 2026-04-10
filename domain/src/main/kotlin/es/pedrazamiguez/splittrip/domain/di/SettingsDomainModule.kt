package es.pedrazamiguez.splittrip.domain.di

import es.pedrazamiguez.splittrip.domain.repository.BalancePreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetSelectedGroupCurrencyUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetSelectedGroupIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetSelectedGroupNameUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.IsOnboardingCompleteUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetOnboardingCompleteUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetSelectedGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetUserDefaultCurrencyUseCase
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

    factory<GetSelectedGroupCurrencyUseCase> {
        GetSelectedGroupCurrencyUseCase(
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
