package es.pedrazamiguez.expenseshareapp.ui.balance.di

import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.usecase.GetBalancesUseCase
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.impl.BalancesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.screen.impl.BalancesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.viewmodel.BalanceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val balancesUiModule = module {
    viewModel { BalanceViewModel(getBalances = get<GetBalancesUseCase>()) }

    factory { BalancesNavigationProviderImpl(onNavigateToGroup = {}) } bind NavigationProvider::class
    single { BalancesScreenUiProviderImpl() } bind ScreenUiProvider::class
}
