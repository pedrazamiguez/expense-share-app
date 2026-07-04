package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.constant.DomainConstants
import es.pedrazamiguez.splittrip.domain.enums.CashWithdrawalReason
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.ResolveCashOnLeaveUseCase
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID

class ResolveCashOnLeaveUseCaseImpl(
    private val cashWithdrawalRepository: CashWithdrawalRepository
) : ResolveCashOnLeaveUseCase {

    override suspend fun invoke(
        groupId: String,
        userId: String,
        memberBalance: MemberBalance,
        groupCurrency: String
    ): Result<Unit> = runCatching {
        when {
            memberBalance.cashInHand == 0L -> Unit // no-op

            memberBalance.cashInHand > 0L -> {
                // For each currency bucket, create a negative CashWithdrawal (LEAVE_DEPOSIT).
                // Effect: cashInHand -= bucket.equivalentCents, pocketBalance += bucket.equivalentCents
                // totalBalance is unchanged; only its composition shifts.
                memberBalance.cashInHandByCurrency
                    .filter { it.amountCents > 0 }
                    .forEach { bucket ->
                        val blendedRate = BigDecimal(bucket.equivalentCents)
                            .divide(
                                BigDecimal(bucket.amountCents),
                                DomainConstants.RATE_PRECISION,
                                RoundingMode.HALF_UP
                            )
                        val deposit = CashWithdrawal(
                            id = UUID.randomUUID().toString(),
                            groupId = groupId,
                            withdrawnBy = userId,
                            createdBy = userId,
                            withdrawalScope = PayerType.USER,
                            amountWithdrawn = -bucket.amountCents,
                            remainingAmount = -bucket.amountCents,
                            currency = bucket.currency,
                            deductedBaseAmount = -bucket.equivalentCents,
                            exchangeRate = blendedRate,
                            reason = CashWithdrawalReason.LEAVE_DEPOSIT,
                            createdAt = LocalDateTime.now(),
                            lastUpdatedAt = LocalDateTime.now()
                        )
                        cashWithdrawalRepository.addWithdrawal(groupId, deposit)
                    }
            }

            else -> {
                // cashInHand < 0 cannot arise in the current codebase
                // (all withdrawal amountWithdrawn values are validated > 0, so cashInHand >= 0 always).
                // Defensive guard; reserved for future LEAVE_REIMBURSEMENT implementation.
                Unit
            }
        }
    }
}
