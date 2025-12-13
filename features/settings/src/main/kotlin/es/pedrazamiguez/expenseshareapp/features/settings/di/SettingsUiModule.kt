package es.pedrazamiguez.expenseshareapp.features.settings.di

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.impl.DefaultCurrencyScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.AppVersionViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.DefaultCurrencyViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.InstallationIdViewModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsUiModule = module {
    viewModel {
        SettingsViewModel(
            authenticationService = get<AuthenticationService>(),
            userPreferences = get<UserPreferences>()
        )
    }
    viewModel { InstallationIdViewModel(cloudMetadataService = get<CloudMetadataService>()) }
    viewModel { AppVersionViewModel(application = get<Application>()) }
    viewModel { DefaultCurrencyViewModel(userPreferences = get<UserPreferences>()) }

    single { DefaultCurrencyScreenUiProviderImpl() } bind ScreenUiProvider::class
}
