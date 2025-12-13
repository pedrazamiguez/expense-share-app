package es.pedrazamiguez.expenseshareapp.features.balance.di

import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetBalancesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.navigation.impl.BalancesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl.BalancesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalanceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val balancesUiModule = module {
    viewModel { BalanceViewModel(getBalances = get<GetBalancesUseCase>()) }

    factory { BalancesNavigationProviderImpl(onNavigateToGroup = {}) } bind NavigationProvider::class
    single { BalancesScreenUiProviderImpl() } bind ScreenUiProvider::class
}
