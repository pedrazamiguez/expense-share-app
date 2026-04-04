package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AddOnCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import java.util.UUID

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val exchangeRateCalculationService: ExchangeRateCalculationService,
    private val groupMembershipService: GroupMembershipService,
    private val contributionRepository: ContributionRepository,
    private val authenticationService: AuthenticationService,
    private val addOnCalculationService: AddOnCalculationService
) {

    suspend operator fun invoke(
        groupId: String?,
        expense: Expense,
        pairedContributionScope: PayerType = PayerType.USER,
        pairedSubunitId: String? = null
    ): Result<Unit> = runCatching {
        require(!groupId.isNullOrBlank()) { "Group ID cannot be null or blank" }
        require(expense.sourceAmount > 0) { "Expense amount must be greater than zero" }
        require(expense.title.isNotBlank()) { "Expense title cannot be empty" }

        groupMembershipService.requireMembership(groupId)

        // Ensure the expense has a stable ID before any processing, so the
        // paired contribution can link to it reliably.
        val expenseWithId = if (expense.id.isBlank()) {
            expense.copy(id = UUID.randomUUID().toString())
        } else {
            expense
        }

        val expenseToSave = if (
            expenseWithId.paymentMethod == PaymentMethod.CASH &&
            expenseWithId.payerType == PayerType.GROUP
        ) {
            processCashExpense(groupId, expenseWithId)
        } else {
            expenseWithId
        }

        expenseRepository.addExpense(groupId, expenseToSave)

        if (expenseToSave.payerType == PayerType.USER) {
            try {
                createPairedContribution(
                    groupId,
                    expenseToSave,
                    pairedContributionScope,
                    pairedSubunitId
                )
            } catch (exception: Exception) {
                runCatching {
                    expenseRepository.deleteExpense(groupId, expenseToSave.id)
                }.exceptionOrNull()?.let { rollbackException ->
                    exception.addSuppressed(rollbackException)
                }
                throw exception
            }
        }
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
            groupAmount = fifoResult.groupAmountCents,
            exchangeRate = exchangeRateCalculationService.calculateBlendedRate(
                sourceAmountCents = expense.sourceAmount,
                groupAmountCents = fifoResult.groupAmountCents
            )
        )
    }

    /**
     * Creates a paired contribution that offsets the out-of-pocket expense in the
     * balance engine. The contribution amount equals the effective group amount
     * (base + add-ons) and its scope/subunit are driven by the caller.
     *
     * Sanitizes the scope/subunit pair before persisting:
     * - SUBUNIT scope requires a non-blank [subunitId] (fail-fast).
     * - GROUP/USER scope forces [subunitId] to null (defensive sanitization).
     *
     * Full subunit membership validation (subunit exists, user is member) is
     * handled at the UI layer via [ContributionValidationService]; adding it here
     * would require injecting SubunitRepository for an I/O read on every expense save.
     */
    private suspend fun createPairedContribution(
        groupId: String,
        expense: Expense,
        contributionScope: PayerType,
        subunitId: String?
    ) {
        val sanitizedSubunitId = sanitizeSubunitId(contributionScope, subunitId)

        val effectiveAmount = addOnCalculationService.calculateEffectiveGroupAmount(
            expense.groupAmount,
            expense.addOns
        )
        val createdBy = authenticationService.requireUserId()
        val userId = expense.payerId ?: createdBy
        val pairedContribution = Contribution(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            userId = userId,
            createdBy = createdBy,
            contributionScope = contributionScope,
            subunitId = sanitizedSubunitId,
            amount = effectiveAmount,
            currency = expense.groupCurrency,
            linkedExpenseId = expense.id
        )
        contributionRepository.addContribution(groupId, pairedContribution)
    }

    /**
     * Validates and sanitizes the scope/subunit pair to prevent invalid
     * contributions from being silently persisted.
     */
    private fun sanitizeSubunitId(
        contributionScope: PayerType,
        subunitId: String?
    ): String? = when (contributionScope) {
        PayerType.SUBUNIT -> {
            require(!subunitId.isNullOrBlank()) {
                "SUBUNIT scope requires a non-blank subunitId"
            }
            subunitId
        }
        else -> null // GROUP/USER must not carry a subunitId
    }
}
