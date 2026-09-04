package es.pedrazamiguez.splittrip.domain.service.featuregate

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.service.ContributionValidationService
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.AddContributionUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.ConfirmSettlementUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.expense.factory.PersistExpenseStrategyFactory
import es.pedrazamiguez.splittrip.domain.usecase.expense.impl.AddExpenseUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.expense.impl.UpdateExpenseUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.expense.strategy.PersistExpenseStrategy
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CoreTransactionalLoopGatingTest {

    private fun assertDoesNotDependOnFeatureGateService(kClass: KClass<*>) {
        val hasFeatureGateServiceParam = kClass.java.constructors.any { constructor ->
            constructor.parameterTypes.any { paramType ->
                paramType == FeatureGateService::class.java
            }
        }
        assertFalse(
            hasFeatureGateServiceParam,
            "${kClass.simpleName} must not depend on FeatureGateService — " +
                "core transactional loop must remain ungated"
        )
    }

    @Test
    fun `core use cases must not have FeatureGateService as constructor dependency`() {
        assertDoesNotDependOnFeatureGateService(AddExpenseUseCaseImpl::class)
        assertDoesNotDependOnFeatureGateService(UpdateExpenseUseCaseImpl::class)
        assertDoesNotDependOnFeatureGateService(AddContributionUseCaseImpl::class)
        assertDoesNotDependOnFeatureGateService(ConfirmSettlementUseCaseImpl::class)
    }

    @Test
    fun `addExpense executes successfully without feature gating`() = runTest {
        val strategyFactory = mockk<PersistExpenseStrategyFactory>()
        val groupRepository = mockk<GroupRepository>()
        val strategy = mockk<PersistExpenseStrategy>()

        val group = Group(id = "group_1", name = "Trip", status = GroupStatus.ACTIVE)
        val expense = Expense(
            id = "expense_1",
            title = "Lunch",
            sourceAmount = 2500L,
            sourceCurrency = "EUR",
            groupAmount = 2500L,
            groupCurrency = "EUR",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        coEvery { groupRepository.getGroupById("group_1") } returns group
        coEvery { strategyFactory.create(isUpdate = false) } returns strategy
        coEvery {
            strategy.persist(
                groupId = "group_1",
                expense = expense,
                pairedContributionScope = PayerType.USER,
                pairedSubunitId = null,
                preferredWithdrawalScope = null,
                preferredWithdrawalOwnerId = null
            )
        } returns Result.success(Unit)

        val useCase = AddExpenseUseCaseImpl(strategyFactory, groupRepository)
        val result = useCase(
            groupId = "group_1",
            expense = expense,
            pairedContributionScope = PayerType.USER,
            pairedSubunitId = null,
            preferredWithdrawalScope = null,
            preferredWithdrawalOwnerId = null
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { strategy.persist("group_1", expense, PayerType.USER, null, null, null) }
    }

    @Test
    fun `updateExpense executes successfully without feature gating`() = runTest {
        val strategyFactory = mockk<PersistExpenseStrategyFactory>()
        val groupRepository = mockk<GroupRepository>()
        val strategy = mockk<PersistExpenseStrategy>()

        val group = Group(id = "group_1", name = "Trip", status = GroupStatus.ACTIVE)
        val expense = Expense(
            id = "expense_1",
            title = "Dinner",
            sourceAmount = 4500L,
            sourceCurrency = "EUR",
            groupAmount = 4500L,
            groupCurrency = "EUR",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        coEvery { groupRepository.getGroupById("group_1") } returns group
        coEvery { strategyFactory.create(isUpdate = true) } returns strategy
        coEvery {
            strategy.persist(
                groupId = "group_1",
                expense = expense,
                pairedContributionScope = PayerType.USER,
                pairedSubunitId = null,
                preferredWithdrawalScope = null,
                preferredWithdrawalOwnerId = null
            )
        } returns Result.success(Unit)

        val useCase = UpdateExpenseUseCaseImpl(strategyFactory, groupRepository)
        val result = useCase(
            groupId = "group_1",
            expense = expense,
            pairedContributionScope = PayerType.USER,
            pairedSubunitId = null,
            preferredWithdrawalScope = null,
            preferredWithdrawalOwnerId = null
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { strategy.persist("group_1", expense, PayerType.USER, null, null, null) }
    }

    @Test
    fun `addContribution executes successfully without feature gating`() = runTest {
        val contributionRepository = mockk<ContributionRepository>(relaxed = true)
        val groupMembershipService = mockk<GroupMembershipService>(relaxed = true)
        val contributionValidationService = mockk<ContributionValidationService>(relaxed = true)
        val subunitRepository = mockk<SubunitRepository>()
        val authenticationService = mockk<AuthenticationService>()
        val groupRepository = mockk<GroupRepository>()

        val group = Group(id = "group_1", name = "Trip", status = GroupStatus.ACTIVE)
        val contribution = Contribution(
            id = "contrib_1",
            groupId = "group_1",
            amount = 10000L,
            currency = "EUR"
        )

        coEvery { groupRepository.getGroupById("group_1") } returns group
        coEvery { groupMembershipService.requireMembership(any()) } just Runs
        every { authenticationService.requireUserId() } returns "user_free"
        coEvery { subunitRepository.getGroupSubunits(any()) } returns emptyList()
        every { contributionValidationService.validateAmount(any()) } returns
            ContributionValidationService.ValidationResult.Valid
        every { contributionValidationService.validate(contribution) } returns
            ContributionValidationService.ValidationResult.Valid

        val useCase = AddContributionUseCaseImpl(
            contributionRepository = contributionRepository,
            groupMembershipService = groupMembershipService,
            contributionValidationService = contributionValidationService,
            subunitRepository = subunitRepository,
            authenticationService = authenticationService,
            groupRepository = groupRepository
        )

        useCase("group_1", contribution)

        coVerify(exactly = 1) { groupMembershipService.requireMembership("group_1") }
        coVerify(exactly = 1) { contributionValidationService.validateAmount(10000L) }
        coVerify(exactly = 1) { contributionRepository.addContribution("group_1", contribution) }
    }

    @Test
    fun `confirmSettlement executes successfully without feature gating`() = runTest {
        val settlementRepository = mockk<SettlementRepository>()
        val authenticationService = mockk<AuthenticationService>()
        val groupRepository = mockk<GroupRepository>()
        val contributionRepository = mockk<ContributionRepository>(relaxed = true)
        val groupDashboardDataSource = mockk<GroupDashboardDataSource>()
        val getMemberBalancesFlowUseCase = mockk<GetMemberBalancesFlowUseCase>()

        val payerId = "user_payer"
        val payeeId = "user_payee"
        val creatorId = "creator_user"
        val group = Group(
            id = "group_1",
            name = "Trip",
            status = GroupStatus.ACTIVE,
            createdBy = creatorId,
            members = listOf(payerId, payeeId, creatorId)
        )
        val settlement = Settlement(
            fromUserId = payerId,
            toUserId = payeeId,
            amount = 3000L,
            currency = "EUR",
            sourcePocket = SettlementPocketType.POCKET
        )
        val settlementRecord = SettlementRecord(
            id = "settlement_1",
            groupId = "group_1",
            settlement = settlement,
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )

        coEvery { groupRepository.getGroupById("group_1") } returns group
        coEvery { settlementRepository.getSettlementById("settlement_1") } returns settlementRecord
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit
        every { authenticationService.requireUserId() } returns payerId

        val useCase = ConfirmSettlementUseCaseImpl(
            settlementRepository = settlementRepository,
            authenticationService = authenticationService,
            groupRepository = groupRepository,
            contributionRepository = contributionRepository,
            groupDashboardDataSource = groupDashboardDataSource,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase
        )

        val result = useCase("group_1", "settlement_1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
    }
}
