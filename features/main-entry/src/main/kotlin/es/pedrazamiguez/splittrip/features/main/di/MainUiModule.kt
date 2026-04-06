package es.pedrazamiguez.splittrip.features.main.di

import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.splittrip.features.main.navigation.DeepLinkHolder
import es.pedrazamiguez.splittrip.features.main.presentation.viewmodel.MainViewModel
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
