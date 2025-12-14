package es.pedrazamiguez.expenseshareapp.features.expense.di

import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.navigation.impl.ExpensesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl.AddExpenseScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl.ExpensesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.AddExpenseViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.ListGroupExpensesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val expensesUiModule = module {
    viewModel {
        ListGroupExpensesViewModel(
            getGroupExpensesFlowUseCase = get<GetGroupExpensesFlowUseCase>(),
            sharedViewModel = get<SharedViewModel>()
        )
    }
    viewModel { AddExpenseViewModel(addExpenseUseCase = get<AddExpenseUseCase>()) }

    factory { ExpensesNavigationProviderImpl() } bind NavigationProvider::class

    single { ExpensesScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddExpenseScreenUiProviderImpl() } bind ScreenUiProvider::class
}
