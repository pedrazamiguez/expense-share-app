package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import org.koin.dsl.module

val expensesDomainModule = module {
    factory<AddExpenseUseCase> { AddExpenseUseCase(expenseRepository = get<ExpenseRepository>()) }
}
