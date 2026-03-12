package es.pedrazamiguez.expenseshareapp.features.settings.di

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignOutUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.GetNotificationPreferencesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UpdateNotificationPreferenceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.impl.DefaultCurrencyScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.impl.NotificationPreferencesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.AppVersionViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.DefaultCurrencyViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.InstallationIdViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.NotificationPreferencesViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsUiModule = module {

    viewModel {
        SettingsViewModel(
            signOutUseCase = get<SignOutUseCase>(),
            getUserDefaultCurrencyUseCase = get<GetUserDefaultCurrencyUseCase>(),
        )
    }

    viewModel { InstallationIdViewModel(cloudMetadataService = get<CloudMetadataService>()) }
    viewModel { AppVersionViewModel(application = get<Application>()) }

    viewModel {
        DefaultCurrencyViewModel(
            getUserDefaultCurrencyUseCase = get<GetUserDefaultCurrencyUseCase>(),
            setUserDefaultCurrencyUseCase = get<SetUserDefaultCurrencyUseCase>(),
        )
    }

    viewModel {
        NotificationPreferencesViewModel(
            getNotificationPreferencesUseCase = get<GetNotificationPreferencesUseCase>(),
            updateNotificationPreferenceUseCase = get<UpdateNotificationPreferenceUseCase>(),
        )
    }

    single { DefaultCurrencyScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { NotificationPreferencesScreenUiProviderImpl() } bind ScreenUiProvider::class
}
