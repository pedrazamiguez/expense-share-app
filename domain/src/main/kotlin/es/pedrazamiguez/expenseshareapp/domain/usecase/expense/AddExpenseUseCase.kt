package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val expenseCalculatorService: ExpenseCalculatorService
) {

    suspend operator fun invoke(
        groupId: String?,
        expense: Expense
    ): Result<Unit> = runCatching {

        require(!groupId.isNullOrBlank()) { "Group ID cannot be null or blank" }
        require(expense.sourceAmount > 0) { "Expense amount must be greater than zero" }
        require(expense.title.isNotBlank()) { "Expense title cannot be empty" }

        val expenseToSave = if (expense.paymentMethod == PaymentMethod.CASH) {
            processCashExpense(groupId, expense)
        } else {
            expense
        }

        expenseRepository.addExpense(groupId, expenseToSave)
    }

    /**
     * Processes a cash expense using FIFO logic:
     * 1. Fetches available withdrawals for the expense currency.
     * 2. Applies FIFO to determine which withdrawals fund the expense.
     * 3. Updates the remaining balance on each consumed withdrawal.
     * 4. Returns the expense with cash tranches and blended group amount attached.
     */
    private suspend fun processCashExpense(groupId: String, expense: Expense): Expense {
        val availableWithdrawals = cashWithdrawalRepository.getAvailableWithdrawals(
            groupId,
            expense.sourceCurrency
        )

        val fifoResult = expenseCalculatorService.calculateFifoCashAmount(
            amountToCover = expense.sourceAmount,
            availableWithdrawals = availableWithdrawals
        )

        // Update remaining amounts on consumed withdrawals
        for (tranche in fifoResult.tranches) {
            val withdrawal = availableWithdrawals.first { it.id == tranche.withdrawalId }
            val newRemaining = withdrawal.remainingAmount - tranche.amountConsumed
            cashWithdrawalRepository.updateRemainingAmount(tranche.withdrawalId, newRemaining)
        }

        return expense.copy(
            cashTranches = fifoResult.tranches,
            groupAmount = fifoResult.groupAmountCents
        )
    }
}
