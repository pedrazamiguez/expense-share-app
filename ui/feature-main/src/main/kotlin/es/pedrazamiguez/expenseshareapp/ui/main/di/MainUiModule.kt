package es.pedrazamiguez.expenseshareapp.ui.main.di

import es.pedrazamiguez.expenseshareapp.ui.main.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainUiModule = module {
    viewModel { MainViewModel() }
}
