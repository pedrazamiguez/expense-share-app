package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel()
