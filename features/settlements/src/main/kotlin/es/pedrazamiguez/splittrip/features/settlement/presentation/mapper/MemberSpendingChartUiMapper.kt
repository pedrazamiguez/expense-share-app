package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.enums.SelfIdentificationContextEnum
import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingBarUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingChartUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SpilloverSegment
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.collections.immutable.toImmutableList

class MemberSpendingChartUiMapper(
    private val localeProvider: LocaleProvider,
    private val userUiMapper: UserUiMapper
) {
    fun toChartUiModel(
        memberBalances: List<MemberBalance>,
        cashOnly: Boolean,
        currentUserId: String?,
        memberProfiles: Map<String, User>,
        groupCurrencyCode: String
    ): MemberSpendingChartUiModel {
        val sortedMembers = memberBalances.sortedWith(
            compareByDescending<MemberBalance> { it.userId == currentUserId }
                .thenBy { resolveDisplayName(it.userId, memberProfiles, currentUserId) }
        )

        val capacities = mutableListOf<Pair<Int, Long>>()
        val overspenders = mutableListOf<Pair<Int, Long>>()
        val ownSpends = mutableMapOf<Int, Long>()
        val allowances = mutableMapOf<Int, Long>()

        var totalAllowance = 0L

        sortedMembers.forEachIndexed { index, balance ->
            val allowanceCents = if (cashOnly) balance.withdrawn else balance.contributed
            val spendingCents = if (cashOnly) balance.cashSpent else balance.totalSpent
            val ownSpendingCents = minOf(spendingCents, allowanceCents)
            val overspentCents = maxOf(0L, spendingCents - allowanceCents)

            allowances[index] = allowanceCents
            ownSpends[index] = ownSpendingCents
            totalAllowance += allowanceCents

            if (overspentCents > 0) {
                overspenders.add(index to overspentCents)
            }
            val available = allowanceCents - ownSpendingCents
            if (available > 0) {
                capacities.add(index to available)
            }
        }

        val spilloverAllocations = buildSpilloverAllocations(overspenders, capacities)

        val bars = sortedMembers.mapIndexed { index, balance ->
            MemberSpendingBarUiModel(
                userId = balance.userId,
                displayName = resolveDisplayName(balance.userId, memberProfiles, currentUserId),
                isCurrentUser = balance.userId == currentUserId,
                allowanceCents = allowances[index] ?: 0L,
                formattedAllowance = formatCurrencyAmount(
                    amount = allowances[index] ?: 0L,
                    currencyCode = groupCurrencyCode,
                    locale = localeProvider.getCurrentLocale()
                ),
                formattedTotalSpent = formatCurrencyAmount(
                    amount = if (cashOnly) balance.cashSpent else balance.totalSpent,
                    currencyCode = groupCurrencyCode,
                    locale = localeProvider.getCurrentLocale()
                ),
                ownSpendingCents = ownSpends[index] ?: 0L,
                spilloverSegments = (spilloverAllocations[index] ?: emptyList()).toImmutableList(),
                memberColorIndex = index
            )
        }.toImmutableList()

        return MemberSpendingChartUiModel(
            bars = bars,
            formattedGroupTotal = formatCurrencyAmount(
                amount = totalAllowance,
                currencyCode = groupCurrencyCode,
                locale = localeProvider.getCurrentLocale()
            ),
            isCashOnly = cashOnly
        )
    }

    private fun buildSpilloverAllocations(
        overspenders: List<Pair<Int, Long>>,
        capacities: MutableList<Pair<Int, Long>>
    ): Map<Int, List<SpilloverSegment>> {
        val spilloverAllocations = mutableMapOf<Int, MutableList<SpilloverSegment>>()
        for ((ownerIndex, amountToDistribute) in overspenders) {
            if (capacities.isEmpty()) continue

            val distribution = distributeSpillover(amountToDistribute, capacities)
            for ((receiverIndex, amount) in distribution) {
                val list = spilloverAllocations.getOrPut(receiverIndex) { mutableListOf() }
                list.add(SpilloverSegment(ownerColorIndex = ownerIndex, amountCents = amount))
                updateCapacity(receiverIndex, amount, capacities)
            }
        }
        return spilloverAllocations
    }

    private fun updateCapacity(receiverIndex: Int, amount: Long, capacities: MutableList<Pair<Int, Long>>) {
        val capIndex = capacities.indexOfFirst { it.first == receiverIndex }
        if (capIndex == -1) return

        val newCap = capacities[capIndex].second - amount
        if (newCap > 0) {
            capacities[capIndex] = receiverIndex to newCap
        } else {
            capacities.removeAt(capIndex)
        }
    }

    private fun resolveDisplayName(
        userId: String,
        memberProfiles: Map<String, User>,
        currentUserId: String?
    ): String {
        return userUiMapper.mapToDisplayName(
            user = memberProfiles[userId],
            fallbackUserId = userId,
            currentUserId = currentUserId,
            selfIdentificationContext = SelfIdentificationContextEnum.NOMINATIVE
        )
    }

    private fun distributeSpillover(
        overspentCents: Long,
        availableSlots: List<Pair<Int, Long>>
    ): Map<Int, Long> {
        if (availableSlots.isEmpty() || overspentCents <= 0) return emptyMap()

        val activeSlots = availableSlots.filter { it.second > 0 }
        if (activeSlots.isEmpty()) return emptyMap()

        val allocation = mutableMapOf<Int, Long>()
        var remainingToDistribute = overspentCents

        val totalAvailableCapacity = activeSlots.sumOf { it.second }
        val amountToDistributeThisRound = minOf(remainingToDistribute, totalAvailableCapacity)

        val slotsCount = BigDecimal(activeSlots.size)
        val sharePerSlot = BigDecimal(amountToDistributeThisRound)
            .divide(slotsCount, 0, RoundingMode.HALF_UP)
            .toLong()

        var remainder = amountToDistributeThisRound - (sharePerSlot * activeSlots.size)

        for (slot in activeSlots) {
            val receiverIndex = slot.first
            var grant = sharePerSlot
            if (remainder > 0) {
                grant += 1
                remainder -= 1
            } else if (remainder < 0) {
                grant -= 1
                remainder += 1
            }
            val availableCapacity = slot.second
            val finalGrant = minOf(grant, availableCapacity)
            allocation[receiverIndex] = finalGrant
        }

        return allocation
    }
}
