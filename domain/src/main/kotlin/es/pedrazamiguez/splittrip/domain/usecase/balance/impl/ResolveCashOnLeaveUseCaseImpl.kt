package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.enums.CashWithdrawalReason
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ResolveCashOnLeaveUseCase
import es.pedrazamiguez.splittrip.domain.usecase.currency.GetExchangeRateUseCase
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ResolveCashOnLeaveUseCaseImpl(
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val exchangeRateCalculationService: ExchangeRateCalculationService,
    private val getExchangeRateUseCase: GetExchangeRateUseCase
) : ResolveCashOnLeaveUseCase {

    override suspend fun invoke(
        groupId: String,
        userId: String,
        memberBalance: MemberBalance,
        groupCurrency: String
    ): Result<Unit> = runCatching {
        when {
            memberBalance.cashInHand == 0L -> Unit

            memberBalance.cashInHand > 0L -> {
                memberBalance.cashInHandByCurrency
                    .filter { it.amountCents > 0 }
                    .forEach { bucket ->
                        resolveBucket(groupId, userId, bucket, groupCurrency)
                    }
            }

            else -> Unit
        }
    }

    private suspend fun resolveBucket(
        groupId: String,
        userId: String,
        bucket: CurrencyAmount,
        groupCurrency: String
    ) {
        val isForeign = bucket.currency != groupCurrency
        val rate: BigDecimal
        val groupAmountCents: Long

        if (!isForeign) {
            rate = BigDecimal.ONE
            groupAmountCents = bucket.equivalentCents
        } else {
            rate = computeBlendedRate(groupId, userId, bucket, groupCurrency)
            groupAmountCents = exchangeRateCalculationService.calculateGroupAmount(
                sourceAmount = BigDecimal(bucket.amountCents),
                rate = rate,
                targetDecimalPlaces = 0
            ).toLong()
        }

        val deposit = CashWithdrawal(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            withdrawnBy = userId,
            createdBy = userId,
            withdrawalScope = PayerType.USER,
            amountWithdrawn = -bucket.amountCents,
            remainingAmount = -bucket.amountCents,
            currency = bucket.currency,
            deductedBaseAmount = -groupAmountCents,
            exchangeRate = rate,
            reason = CashWithdrawalReason.LEAVE_DEPOSIT,
            createdAt = LocalDateTime.now(),
            lastUpdatedAt = LocalDateTime.now()
        )
        cashWithdrawalRepository.addWithdrawal(groupId, deposit)
    }

    private suspend fun computeBlendedRate(
        groupId: String,
        userId: String,
        bucket: CurrencyAmount,
        groupCurrency: String
    ): BigDecimal {
        val availableWithdrawals = cashWithdrawalRepository.getAvailableWithdrawals(
            groupId = groupId,
            currency = bucket.currency,
            payerType = PayerType.USER,
            payerId = userId
        )

        if (availableWithdrawals.isNotEmpty()) {
            return exchangeRateCalculationService.calculateBlendedRate(
                sourceAmountCents = bucket.amountCents,
                groupAmountCents = bucket.equivalentCents
            )
        }

        val rateResult = getExchangeRateUseCase(
            baseCurrencyCode = groupCurrency,
            targetCurrencyCode = bucket.currency
        )
        return if (rateResult != null) {
            exchangeRateCalculationService.displayRateToCalculationRate(rateResult.rate.toPlainString())
        } else {
            BigDecimal.ONE
        }
    }
}
