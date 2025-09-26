package es.pedrazamiguez.expenseshareapp.ui.expense.di

import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.impl.ExpensesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.screen.impl.ExpensesScreenUiProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val expensesUiModule = module {
    factory { ExpensesNavigationProviderImpl() } bind NavigationProvider::class
    single { ExpensesScreenUiProviderImpl() } bind ScreenUiProvider::class
}
