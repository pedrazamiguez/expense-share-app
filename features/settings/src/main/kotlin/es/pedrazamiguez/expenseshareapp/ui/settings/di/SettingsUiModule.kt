package es.pedrazamiguez.expenseshareapp.ui.settings.di

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.impl.DefaultCurrencyScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.AppVersionViewModel
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.DefaultCurrencyViewModel
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.InstallationIdViewModel
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
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
