package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy.SettlementConfirmationStrategyFactory
import es.pedrazamiguez.splittrip.domain.usecase.balance.strategy.StandardContributionAttributionStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.MemberBalanceCalculationInputs
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.first

class ConfirmSettlementUseCaseImpl(
    private val settlementRepository: SettlementRepository,
    private val authenticationService: AuthenticationService,
    private val groupRepository: GroupRepository,
    private val contributionRepository: ContributionRepository,
    private val groupDashboardDataSource: GroupDashboardDataSource,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase
) : ConfirmSettlementUseCase {

    @Suppress("LongMethod")
    override suspend operator fun invoke(
        groupId: String,
        settlementId: String
    ): Result<SettlementRecord> = runCatching {
        val currentUserId = authenticationService.requireUserId()
        val record = settlementRepository.getSettlementById(settlementId)
            ?: throw IllegalArgumentException("Settlement not found: $settlementId")

        val group = groupRepository.getGroupById(groupId)
            ?: throw IllegalArgumentException("Group not found: $groupId")
        val isCreator = group.createdBy == currentUserId

        val strategy = SettlementConfirmationStrategyFactory.getStrategy(record)
        val updated = strategy.confirm(record, currentUserId, isCreator)

        settlementRepository.updateSettlement(updated)

        if (updated.status == SettlementStatus.RESOLVED) {
            var equivalentBaseAmount: Long? = null
            var exchangeRate: BigDecimal? = null

            if (record.settlement.currency != group.currency) {
                val dashboardData = groupDashboardDataSource.getDashboardSnapshotFlow(groupId).first()
                val inputs = MemberBalanceCalculationInputs(
                    contributions = dashboardData.contributions,
                    withdrawals = dashboardData.withdrawals,
                    expenses = dashboardData.expenses,
                    subunits = dashboardData.subunits,
                    groupMemberIds = group.members,
                    groupCurrency = group.currency,
                    settlements = dashboardData.settlements,
                    attributionStrategy = StandardContributionAttributionStrategy
                )
                val balances = getMemberBalancesFlowUseCase.computeMemberBalances(inputs)

                val payerBalance = balances.find { it.userId == record.settlement.fromUserId }
                val currencyAmount = payerBalance?.cashInHandByCurrency?.find {
                    it.currency == record.settlement.currency
                }
                if (currencyAmount != null && currencyAmount.equivalentCents > 0L) {
                    val rate = BigDecimal(currencyAmount.amountCents).divide(
                        BigDecimal(currencyAmount.equivalentCents),
                        MathContext.DECIMAL128
                    )
                    exchangeRate = rate
                    equivalentBaseAmount = BigDecimal(record.settlement.amount)
                        .divide(rate, MathContext.DECIMAL128)
                        .setScale(0, RoundingMode.HALF_UP)
                        .toLong()
                }
            }

            val settlementContribution = Contribution(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                userId = record.settlement.fromUserId,
                createdBy = currentUserId,
                contributionScope = PayerType.USER,
                amount = record.settlement.amount,
                currency = record.settlement.currency,
                equivalentBaseAmount = equivalentBaseAmount,
                exchangeRate = exchangeRate,
                linkedSettlementId = record.id,
                createdAt = updated.resolvedAt ?: LocalDateTime.now()
            )
            contributionRepository.addContribution(groupId, settlementContribution)
        }

        updated
    }
}
