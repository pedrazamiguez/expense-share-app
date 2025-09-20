package es.pedrazamiguez.expenseshareapp.ui.balance.di

import es.pedrazamiguez.expenseshareapp.domain.usecase.GetBalancesUseCase
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.viewmodel.BalanceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val balanceModule = module {
    viewModel { BalanceViewModel(getBalances = get<GetBalancesUseCase>()) }
}
