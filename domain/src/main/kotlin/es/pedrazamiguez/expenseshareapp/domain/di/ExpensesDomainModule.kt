package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import org.koin.dsl.module

val expensesDomainModule = module {
    factory<AddExpenseUseCase> { AddExpenseUseCase(expenseRepository = get<ExpenseRepository>()) }
    factory<GetGroupExpensesFlowUseCase> { GetGroupExpensesFlowUseCase(expenseRepository = get<ExpenseRepository>()) }
    factory<GetGroupExpenseConfigUseCase> {
        GetGroupExpenseConfigUseCase(
            groupRepository = get<GroupRepository>(),
            currencyRepository = get<CurrencyRepository>()
        )
    }
}
