package es.pedrazamiguez.expenseshareapp.core.designsystem.di

import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupNameUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetSelectedGroupUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreDesignSystemModule = module {

    viewModel {
        SharedViewModel(
            getSelectedGroupIdUseCase = get<GetSelectedGroupIdUseCase>(),
            getSelectedGroupNameUseCase = get<GetSelectedGroupNameUseCase>(),
            setSelectedGroupUseCase = get<SetSelectedGroupUseCase>(),
        )
    }

}
