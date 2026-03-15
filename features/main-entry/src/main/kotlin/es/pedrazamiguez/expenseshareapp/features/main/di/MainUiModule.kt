package es.pedrazamiguez.expenseshareapp.features.main.di

import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.features.main.navigation.DeepLinkHolder
import es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainUiModule = module {
    single { DeepLinkHolder() }

    viewModel {
        MainViewModel(
            registerDeviceTokenUseCase = get<RegisterDeviceTokenUseCase>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>()
        )
    }
}
