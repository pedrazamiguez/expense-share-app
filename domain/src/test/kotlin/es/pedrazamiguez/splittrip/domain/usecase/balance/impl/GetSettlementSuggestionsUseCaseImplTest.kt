package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetSettlementSuggestionsUseCaseImplTest {

    private val debtSimplificationService = mockk<DebtSimplificationService>()
    private val settlementRepository = mockk<SettlementRepository>()
    private val groupRepository = mockk<GroupRepository>()
    private val expenseRepository = mockk<ExpenseRepository>()
    private val contributionRepository = mockk<ContributionRepository>()
    private val cashWithdrawalRepository = mockk<CashWithdrawalRepository>()
    private val subunitRepository = mockk<SubunitRepository>()
    private val getMemberBalancesFlowUseCase = mockk<GetMemberBalancesFlowUseCase>()
    private val useCase = GetSettlementSuggestionsUseCaseImpl(
        debtSimplificationService = debtSimplificationService,
        settlementRepository = settlementRepository,
        groupRepository = groupRepository,
        expenseRepository = expenseRepository,
        contributionRepository = contributionRepository,
        cashWithdrawalRepository = cashWithdrawalRepository,
        subunitRepository = subunitRepository,
        getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase
    )

    private val groupId = "group-1"
    private val memberBalances = listOf(
        MemberBalance(userId = "1", pocketBalance = -1000, cashInHand = 0),
        MemberBalance(userId = "2", pocketBalance = 1000, cashInHand = 0)
    )

    private fun mockBaseFlowRepos(currency: String = "EUR") {
        val group = mockk<Group>(relaxed = true).apply {
            every { members } returns listOf("user-1", "user-2")
            every { this@apply.currency } returns currency
        }
        coEvery { groupRepository.getGroupById(groupId) } returns group
        every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
        every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
        every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
        coEvery { subunitRepository.getGroupSubunits(groupId) } returns emptyList()
        coEvery { getMemberBalancesFlowUseCase.computeMemberBalances(any(), any(), any(), any(), any(), any()) } returns
            memberBalances
    }

    @Test
    fun `invoke delegates to DebtSimplificationService`() {
        val balances = listOf(
            MemberBalance(userId = "1", pocketBalance = -1000, cashInHand = 0),
            MemberBalance(userId = "2", pocketBalance = 1000, cashInHand = 0)
        )
        val expected = listOf(Settlement(fromUserId = "1", toUserId = "2", amount = 1000L))
        every { debtSimplificationService.simplify(balances) } returns expected

        val result = useCase(balances)

        assertEquals(expected, result)
        verify(exactly = 1) { debtSimplificationService.simplify(balances) }
    }

    @Test
    fun `invokeByPocket delegates to DebtSimplificationService simplifyByPocket with groupCurrency`() {
        val balances = listOf(
            MemberBalance(userId = "1", pocketBalance = 500, cashInHand = -300),
            MemberBalance(userId = "2", pocketBalance = -500, cashInHand = 300)
        )
        val expected = listOf(Settlement(fromUserId = "1", toUserId = "2", amount = 200L))
        every { debtSimplificationService.simplifyByPocket(balances, "EUR") } returns expected
        val result = useCase.invokeByPocket(balances, "EUR")

        assertEquals(expected, result)
        verify(exactly = 1) { debtSimplificationService.simplifyByPocket(balances, "EUR") }
    }

    @Test
    fun `persistForGroup filters out NET and persists only CASH settlements`() = runTest {
        val cashSettlement =
            Settlement(
                fromUserId = "2",
                toUserId = "1",
                amount = 500L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            )
        val netSettlement =
            Settlement(
                fromUserId = "1",
                toUserId = "2",
                amount = 1000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.NET
            )
        every { debtSimplificationService.simplifyByPocket(memberBalances, "EUR") } returns
            listOf(netSettlement, cashSettlement)
        mockBaseFlowRepos()

        val expectedRecord =
            SettlementRecord(
                id = "new-1",
                groupId = groupId,
                settlement = cashSettlement,
                status = SettlementStatus.SUGGESTED,
                createdAt = LocalDateTime.now()
            )
        coEvery { settlementRepository.getGroupSettlements(groupId) } returnsMany
            listOf(emptyList(), listOf(expectedRecord))
        coEvery { settlementRepository.addSettlement(any()) } returns Unit

        val result = useCase.persistForGroup(groupId)

        coVerify(exactly = 1) {
            settlementRepository.addSettlement(
                match {
                    it.settlement.sourcePocket ==
                        SettlementPocketType.CASH
                }
            )
        }
        coVerify(exactly = 0) {
            settlementRepository.addSettlement(
                match {
                    it.settlement.sourcePocket ==
                        SettlementPocketType.NET
                }
            )
        }
        assertEquals(1, result.size)
        assertEquals(SettlementStatus.SUGGESTED, result.first().status)
    }

    @Test
    fun `persistForGroup skips pairs with existing non-RESOLVED records`() = runTest {
        val existingSettlement =
            Settlement(
                fromUserId = "1",
                toUserId = "2",
                amount = 1000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            )
        every { debtSimplificationService.simplifyByPocket(memberBalances, "EUR") } returns listOf(existingSettlement)
        mockBaseFlowRepos()

        val existingRecord =
            SettlementRecord(
                id = "existing-1",
                groupId = groupId,
                settlement = existingSettlement,
                status = SettlementStatus.CONFIRMED_BY_PAYER,
                createdAt = LocalDateTime.now()
            )
        coEvery { settlementRepository.getGroupSettlements(groupId) } returns listOf(existingRecord)

        useCase.persistForGroup(groupId)

        coVerify(exactly = 0) { settlementRepository.addSettlement(any()) }
    }

    @Test
    fun `persistForGroup updates existing SUGGESTED record when details changed`() = runTest {
        val existingSettlement =
            Settlement(
                fromUserId = "1",
                toUserId = "2",
                amount = 1000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            )
        val computedSettlement = existingSettlement.copy(amount = 1500L)
        every { debtSimplificationService.simplifyByPocket(memberBalances, "EUR") } returns listOf(computedSettlement)
        mockBaseFlowRepos()

        val existingRecord =
            SettlementRecord(
                id = "existing-1",
                groupId = groupId,
                settlement = existingSettlement,
                status = SettlementStatus.SUGGESTED,
                createdAt = LocalDateTime.now()
            )
        coEvery { settlementRepository.getGroupSettlements(groupId) } returnsMany listOf(
            listOf(existingRecord),
            listOf(existingRecord.copy(settlement = computedSettlement))
        )
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase.persistForGroup(groupId)

        coVerify(exactly = 0) { settlementRepository.addSettlement(any()) }
        coVerify(exactly = 1) {
            settlementRepository.updateSettlement(
                match {
                    it.id == "existing-1" && it.settlement.amount == 1500L
                }
            )
        }
        assertEquals(1, result.size)
        assertEquals(1500L, result.first().settlement.amount)
    }

    @Test
    fun `persistForGroup skips existing SUGGESTED record when details did not change`() = runTest {
        val existingSettlement =
            Settlement(
                fromUserId = "1",
                toUserId = "2",
                amount = 1000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            )
        every { debtSimplificationService.simplifyByPocket(memberBalances, "EUR") } returns listOf(existingSettlement)
        mockBaseFlowRepos()

        val existingRecord =
            SettlementRecord(
                id = "existing-1",
                groupId = groupId,
                settlement = existingSettlement,
                status = SettlementStatus.SUGGESTED,
                createdAt = LocalDateTime.now()
            )
        coEvery { settlementRepository.getGroupSettlements(groupId) } returns listOf(existingRecord)

        useCase.persistForGroup(groupId)

        coVerify(exactly = 0) { settlementRepository.addSettlement(any()) }
        coVerify(exactly = 0) { settlementRepository.updateSettlement(any()) }
    }

    @Test
    fun `persistForGroup returns empty list when group not found`() = runTest {
        coEvery { groupRepository.getGroupById("missing-group") } returns null

        val result = useCase.persistForGroup("missing-group")

        assertEquals(emptyList<SettlementRecord>(), result)
    }

    @Test
    fun `persistForGroup purges obsolete SUGGESTED records when no longer computed`() = runTest {
        val obsoleteRecord = SettlementRecord(
            id = "stale-1",
            groupId = groupId,
            settlement = Settlement(
                fromUserId = "1",
                toUserId = "2",
                amount = 500L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            ),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        every { debtSimplificationService.simplifyByPocket(memberBalances, "EUR") } returns emptyList()
        mockBaseFlowRepos()

        coEvery { settlementRepository.getGroupSettlements(groupId) } returnsMany listOf(
            listOf(obsoleteRecord),
            emptyList()
        )
        coEvery { settlementRepository.deleteSettlement(any()) } returns Unit

        val result = useCase.persistForGroup(groupId)

        coVerify(exactly = 1) { settlementRepository.deleteSettlement(obsoleteRecord) }
        assertEquals(0, result.size)
    }

    @Test
    fun `persistForGroup purges duplicate SUGGESTED records with same business key`() = runTest {
        val settlement = Settlement(
            fromUserId = "1",
            toUserId = "2",
            amount = 1000L,
            currency = "EUR",
            sourcePocket = SettlementPocketType.POCKET
        )
        val primaryRecord = SettlementRecord(
            id = "rec-1",
            groupId = groupId,
            settlement = settlement,
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        val duplicateRecord = SettlementRecord(
            id = "rec-2",
            groupId = groupId,
            settlement = settlement,
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )

        every { debtSimplificationService.simplifyByPocket(memberBalances, "EUR") } returns listOf(settlement)
        mockBaseFlowRepos()

        coEvery { settlementRepository.getGroupSettlements(groupId) } returnsMany listOf(
            listOf(primaryRecord, duplicateRecord),
            listOf(primaryRecord)
        )
        coEvery { settlementRepository.deleteSettlement(duplicateRecord) } returns Unit

        val result = useCase.persistForGroup(groupId)

        coVerify(exactly = 1) { settlementRepository.deleteSettlement(duplicateRecord) }
        assertEquals(1, result.size)
    }
}
