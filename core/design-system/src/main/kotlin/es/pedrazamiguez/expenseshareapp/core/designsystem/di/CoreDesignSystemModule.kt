package es.pedrazamiguez.expenseshareapp.core.designsystem.di

import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreDesignSystemModule = module {
    viewModel { SharedViewModel(userPreferences = get<UserPreferences>()) }
}
