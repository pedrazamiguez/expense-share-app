package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val groupMembershipService: GroupMembershipService
) {

    suspend operator fun invoke(
        groupId: String?,
        expense: Expense
    ): Result<Unit> = runCatching {

        require(!groupId.isNullOrBlank()) { "Group ID cannot be null or blank" }
        require(expense.sourceAmount > 0) { "Expense amount must be greater than zero" }
        require(expense.title.isNotBlank()) { "Expense title cannot be empty" }

        groupMembershipService.requireMembership(groupId)

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
     * 3. Batches all remaining-amount updates into a single local DB transaction + one cloud sync job.
     * 4. Returns the expense with cash tranches and blended group amount attached.
     */
    private suspend fun processCashExpense(groupId: String, expense: Expense): Expense {
        val availableWithdrawals = cashWithdrawalRepository.getAvailableWithdrawals(
            groupId,
            expense.sourceCurrency
        )

        // Guard before delegating to the calculator so we can throw a strongly-typed
        // exception that carries raw cent values for proper formatting in the UI layer.
        if (expenseCalculatorService.hasInsufficientCash(expense.sourceAmount, availableWithdrawals)) {
            val availableCents = availableWithdrawals.sumOf { it.remainingAmount }
            throw InsufficientCashException(
                requiredCents = expense.sourceAmount,
                availableCents = availableCents
            )
        }

        val fifoResult = expenseCalculatorService.calculateFifoCashAmount(
            amountToCover = expense.sourceAmount,
            availableWithdrawals = availableWithdrawals
        )

        // Build updated withdrawal objects directly from the already-in-memory list —
        // no extra DB reads — then persist all in one transaction and one cloud sync job.
        val withdrawalById = availableWithdrawals.associateBy { it.id }
        val updatedWithdrawals = fifoResult.tranches.map { tranche ->
            val withdrawal = withdrawalById.getValue(tranche.withdrawalId)
            withdrawal.copy(remainingAmount = withdrawal.remainingAmount - tranche.amountConsumed)
        }
        cashWithdrawalRepository.updateRemainingAmounts(groupId, updatedWithdrawals)

        return expense.copy(
            cashTranches = fifoResult.tranches,
            groupAmount = fifoResult.groupAmountCents
        )
    }
}
