package es.pedrazamiguez.expenseshareapp.ui.settings.di

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.InstallationIdViewModel
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsUiModule = module {
    viewModel { SettingsViewModel(authenticationService = get<AuthenticationService>()) }
    viewModel { InstallationIdViewModel(cloudMetadataService = get<CloudMetadataService>()) }
}
