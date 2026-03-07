package es.pedrazamiguez.expenseshareapp.features.expense.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.DeleteExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
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


    single {
        AddExpenseUiMapper(
            localeProvider = get<LocaleProvider>(),
            resourceProvider = get<ResourceProvider>()
        )
    }

    single {
        ExpenseUiMapper(
            localeProvider = get<LocaleProvider>(), resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        ExpensesViewModel(
            getGroupExpensesFlowUseCase = get<GetGroupExpensesFlowUseCase>(),
            deleteExpenseUseCase = get<DeleteExpenseUseCase>(),
            expenseUiMapper = get<ExpenseUiMapper>()
        )
    }
    viewModel {
        AddExpenseViewModel(
            addExpenseUseCase = get<AddExpenseUseCase>(),
            getGroupExpenseConfigUseCase = get<GetGroupExpenseConfigUseCase>(),
            getExchangeRateUseCase = get<GetExchangeRateUseCase>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            expenseValidationService = get<ExpenseValidationService>(),
            getGroupLastUsedCurrencyUseCase = get<GetGroupLastUsedCurrencyUseCase>(),
            setGroupLastUsedCurrencyUseCase = get<SetGroupLastUsedCurrencyUseCase>(),
            splitCalculatorFactory = get<ExpenseSplitCalculatorFactory>(),
            addExpenseUiMapper = get<AddExpenseUiMapper>()
        )
    }

    factory { ExpensesNavigationProviderImpl() } bind NavigationProvider::class

    single { ExpensesScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddExpenseScreenUiProviderImpl() } bind ScreenUiProvider::class
}
