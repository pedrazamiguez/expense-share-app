package es.pedrazamiguez.expenseshareapp.features.expense.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.navigation.impl.ExpensesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl.AddExpenseScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl.ExpensesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.AddExpenseViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.ExpensesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val expensesUiModule = module {

    factory { ExpenseCalculatorService() }

    single { AddExpenseUiMapper() }

    single {
        ExpenseUiMapper(
            localeProvider = get<LocaleProvider>(), resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        ExpensesViewModel(
            getGroupExpensesFlowUseCase = get<GetGroupExpensesFlowUseCase>(),
            expenseUiMapper = get<ExpenseUiMapper>()
        )
    }
    viewModel {
        AddExpenseViewModel(
            addExpenseUseCase = get<AddExpenseUseCase>(),
            getGroupExpenseConfigUseCase = get<GetGroupExpenseConfigUseCase>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            addExpenseUiMapper = get<AddExpenseUiMapper>()
        )
    }

    factory { ExpensesNavigationProviderImpl() } bind NavigationProvider::class

    single { ExpensesScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddExpenseScreenUiProviderImpl() } bind ScreenUiProvider::class
}
