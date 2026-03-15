package es.pedrazamiguez.expenseshareapp.features.main.di

import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainUiModule = module {
    viewModel {
        MainViewModel(
            registerDeviceTokenUseCase = get<RegisterDeviceTokenUseCase>()
        )
    }
}
