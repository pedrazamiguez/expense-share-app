package es.pedrazamiguez.expenseshareapp.core.ui.di

import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.viewmodel.SharedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreUiModule = module {
    viewModel { SharedViewModel(userPreferences = get<UserPreferences>()) }
}
