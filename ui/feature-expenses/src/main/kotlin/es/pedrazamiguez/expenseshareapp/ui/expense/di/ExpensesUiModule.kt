package es.pedrazamiguez.expenseshareapp.ui.expense.di

import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.impl.ExpensesNavigationProviderImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val expensesUiModule = module {
    factory { ExpensesNavigationProviderImpl() } bind NavigationProvider::class
}
